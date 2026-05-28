package meteordevelopment.meteorclient.gui.titlemenu;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.util.audio.TitleAudioAnalyzer;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TitleMusicPlayer {
    private static final TitleMusicPlayer INSTANCE = new TitleMusicPlayer();
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();

    private Clip currentClip;
    private boolean isPlaying = false;
    private float volume = 0.6f;
    private FloatControl gainControl;

    // ===== MULTI TRACK SYSTEM =====
    private static final String[] MENU_MUSIC_PATHS = {
        "assets/meteor-client/music/menu_music3.wav",
        "assets/meteor-client/music/menu_music.wav",
        "assets/meteor-client/music/menu_music2.wav"
    };

    private final Random random = new Random();

    private long lastAudioActionTime = 0;
    private static final long MIN_ACTION_INTERVAL = 200;
    private boolean menuMusicActive = false;

    private TitleMusicPlayer() {}

    public static TitleMusicPlayer getInstance() {
        return INSTANCE;
    }

    public void playMenuMusic() {
        try {
            if (menuMusicActive && isPlaying) return;

            long now = System.currentTimeMillis();
            if (now - lastAudioActionTime < MIN_ACTION_INTERVAL) return;
            lastAudioActionTime = now;

            if (isPlaying) stop();

            TitleAudioAnalyzer.getInstance().stopAnalysis();
            TitleAudioAnalyzer.getInstance().startAnalysis();

            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            playRandomTrack();
            menuMusicActive = true;

            MeteorClient.LOG.info("[TitleScreen] Menu music started");
        } catch (Exception e) {
            MeteorClient.LOG.error("[TitleScreen] Failed to start menu music: " + e.getMessage());
        }
    }

    // ===== RANDOM TRACK PICKER =====
    private void playRandomTrack() {
        String selected = MENU_MUSIC_PATHS[random.nextInt(MENU_MUSIC_PATHS.length)];
        playFromResource(selected);
    }

    public void playFromResource(String resourcePath) {
        audioExecutor.submit(() -> {
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
        audioExecutor.submit(() -> {
            try {
                stop();
                menuMusicActive = false;

                File file = new File(filePath);
                if (!file.exists()) {
                    MeteorClient.LOG.error("[TitleScreen] File not found: " + filePath);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(new FileInputStream(file))
                );

                setupAndPlay(audioStream);

                TitleAudioAnalyzer.getInstance().stopAnalysis();
                TitleAudioAnalyzer.getInstance().startAnalysis();
            } catch (Exception e) {
                MeteorClient.LOG.error("[TitleScreen] Failed to play file: " + e.getMessage());
            }
        });
    }

    private void setupAndPlay(AudioInputStream audioStream) {
        try {
            AudioFormat format = audioStream.getFormat();

            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat decoded = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
                );
                audioStream = AudioSystem.getAudioInputStream(decoded, audioStream);
                format = audioStream.getFormat();
            }

            DataLine.Info info = new DataLine.Info(Clip.class, format);

            try {
                currentClip = (Clip) AudioSystem.getLine(info);
            } catch (Exception e) {
                Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
                for (Mixer.Info mixerInfo : mixerInfos) {
                    try {
                        Mixer mixer = AudioSystem.getMixer(mixerInfo);
                        if (mixer.isLineSupported(info)) {
                            currentClip = (Clip) mixer.getLine(info);
                            break;
                        }
                    } catch (Exception ignored) {}
                }
                if (currentClip == null) throw new LineUnavailableException("No suitable audio line found");
            }

            currentClip.open(audioStream);

            if (currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(volume);
            }

            // ===== AUTO PLAY NEXT SONG =====
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    isPlaying = false;

                    if (menuMusicActive) {
                        playRandomTrack(); // 🔥 pick a new song instead of looping
                    }
                }
            });

            currentClip.start();
            isPlaying = true;

        } catch (Exception e) {
            MeteorClient.LOG.error("[TitleScreen] Audio setup error: " + e.getMessage());
        }
    }

    public void stop() {
        long now = System.currentTimeMillis();
        if (now - lastAudioActionTime < MIN_ACTION_INTERVAL) return;
        lastAudioActionTime = now;

        TitleAudioAnalyzer.getInstance().stopAnalysis();

        if (currentClip != null) {
            if (currentClip.isRunning()) currentClip.stop();
            currentClip.close();
            currentClip = null;
        }

        isPlaying = false;
        menuMusicActive = false;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));

        if (gainControl != null) {
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (volume > 0.0f)
                ? (range * volume) + gainControl.getMinimum()
                : gainControl.getMinimum();

            gainControl.setValue(gain);
        }
    }

    public float getVolume() {
        return volume;
    }

    public boolean isPlaying() {
        return isPlaying && currentClip != null && currentClip.isRunning();
    }
}
