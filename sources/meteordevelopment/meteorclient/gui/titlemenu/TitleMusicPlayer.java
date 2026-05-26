package meteordevelopment.meteorclient.gui.titlemenu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.util.audio.TitleAudioAnalyzer;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/titlemenu/TitleMusicPlayer.class */
public class TitleMusicPlayer {
    private Clip currentClip;
    private FloatControl gainControl;
    private static final long MIN_ACTION_INTERVAL = 200;
    private static final TitleMusicPlayer INSTANCE = new TitleMusicPlayer();
    private static final String[] MENU_MUSIC_PATHS = {"assets/meteor-client/music/menu_music3.wav", "assets/meteor-client/music/menu_music.wav", "assets/meteor-client/music/menu_music2.wav"};
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private boolean isPlaying = false;
    private float volume = 0.6f;
    private final Random random = new Random();
    private long lastAudioActionTime = 0;
    private boolean menuMusicActive = false;

    private TitleMusicPlayer() {
    }

    public static TitleMusicPlayer getInstance() {
        return INSTANCE;
    }

    public void playMenuMusic() {
        try {
            if (this.menuMusicActive && this.isPlaying) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now - this.lastAudioActionTime < MIN_ACTION_INTERVAL) {
                return;
            }
            this.lastAudioActionTime = now;
            if (this.isPlaying) {
                stop();
            }
            TitleAudioAnalyzer.getInstance().stopAnalysis();
            TitleAudioAnalyzer.getInstance().startAnalysis();
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            playRandomTrack();
            this.menuMusicActive = true;
            MeteorClient.LOG.info("[TitleScreen] Menu music started");
        } catch (Exception e2) {
            MeteorClient.LOG.error("[TitleScreen] Failed to start menu music: " + e2.getMessage());
        }
    }

    private void playRandomTrack() {
        String selected = MENU_MUSIC_PATHS[this.random.nextInt(MENU_MUSIC_PATHS.length)];
        playFromResource(selected);
    }

    public void playFromResource(String resourcePath) {
        this.audioExecutor.submit(() -> {
            try {
                stop();
                InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (is == null) {
                    MeteorClient.LOG.error("[TitleScreen] Audio resource not found: " + resourcePath);
                    return;
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                setupAndPlay(audioStream);
                MeteorClient.LOG.info("[TitleScreen] Playing: " + resourcePath);
            } catch (Exception e) {
                MeteorClient.LOG.error("[TitleScreen] Failed to play audio: " + e.getMessage());
            }
        });
    }

    public void playFromFile(String filePath) {
        this.audioExecutor.submit(() -> {
            try {
                stop();
                this.menuMusicActive = false;
                File file = new File(filePath);
                if (!file.exists()) {
                    MeteorClient.LOG.error("[TitleScreen] File not found: " + filePath);
                    return;
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
                setupAndPlay(audioStream);
                TitleAudioAnalyzer.getInstance().stopAnalysis();
                TitleAudioAnalyzer.getInstance().startAnalysis();
            } catch (Exception e) {
                MeteorClient.LOG.error("[TitleScreen] Failed to play file: " + e.getMessage());
            }
        });
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: javax.sound.sampled.LineUnavailableException */
    private void setupAndPlay(AudioInputStream audioStream) throws LineUnavailableException {
        try {
            AudioFormat format = audioStream.getFormat();
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat decoded = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), 16, format.getChannels(), format.getChannels() * 2, format.getSampleRate(), false);
                audioStream = AudioSystem.getAudioInputStream(decoded, audioStream);
                format = audioStream.getFormat();
            }
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            try {
                this.currentClip = AudioSystem.getLine(info);
            } catch (Exception e) {
                Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
                for (Mixer.Info mixerInfo : mixerInfos) {
                    try {
                        Mixer mixer = AudioSystem.getMixer(mixerInfo);
                        if (mixer.isLineSupported(info)) {
                            this.currentClip = mixer.getLine(info);
                            break;
                        }
                    } catch (Exception e2) {
                    }
                }
                if (this.currentClip == null) {
                    throw new LineUnavailableException("No suitable audio line found");
                }
            }
            this.currentClip.open(audioStream);
            if (this.currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                this.gainControl = this.currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(this.volume);
            }
            this.currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    this.isPlaying = false;
                    if (this.menuMusicActive) {
                        playRandomTrack();
                    }
                }
            });
            this.currentClip.start();
            this.isPlaying = true;
        } catch (Exception e3) {
            MeteorClient.LOG.error("[TitleScreen] Audio setup error: " + e3.getMessage());
        }
    }

    public void stop() {
        long now = System.currentTimeMillis();
        if (now - this.lastAudioActionTime < MIN_ACTION_INTERVAL) {
            return;
        }
        this.lastAudioActionTime = now;
        TitleAudioAnalyzer.getInstance().stopAnalysis();
        if (this.currentClip != null) {
            if (this.currentClip.isRunning()) {
                this.currentClip.stop();
            }
            this.currentClip.close();
            this.currentClip = null;
        }
        this.isPlaying = false;
        this.menuMusicActive = false;
    }

    public void setVolume(float volume) {
        float minimum;
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        if (this.gainControl != null) {
            float range = this.gainControl.getMaximum() - this.gainControl.getMinimum();
            if (volume > 0.0f) {
                minimum = (range * volume) + this.gainControl.getMinimum();
            } else {
                minimum = this.gainControl.getMinimum();
            }
            float gain = minimum;
            this.gainControl.setValue(gain);
        }
    }

    public float getVolume() {
        return this.volume;
    }

    public boolean isPlaying() {
        return this.isPlaying && this.currentClip != null && this.currentClip.isRunning();
    }
}
