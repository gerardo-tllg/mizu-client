package meteordevelopment.meteorclient.util.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import meteordevelopment.meteorclient.MeteorClient;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/util/audio/TitleAudioAnalyzer.class */
public class TitleAudioAnalyzer {
    private static final TitleAudioAnalyzer INSTANCE = new TitleAudioAnalyzer();
    private static final int BUFFER_SIZE = 4096;
    private static final int SAMPLE_RATE = 44100;
    private static final int FFT_SIZE = 2048;
    private static final int BASS_MAX_FREQ = 250;
    private static final int MID_MAX_FREQ = 2000;
    private static final long PEAK_RESET_TIME = 300;
    private TargetDataLine audioLine;
    private float bassLevel = 0.0f;
    private float trebleLevel = 0.0f;
    private float volumeLevel = 0.0f;
    private float[] spectrum = new float[1024];
    private float bassPeak = 0.0f;
    private float treblePeak = 0.0f;
    private long lastBassPeakTime = 0;
    private long lastTreblePeakTime = 0;
    private boolean bassBeat = false;
    private boolean trebleBeat = false;
    private boolean analyzing = false;
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private final float bassSmoothing = 0.4f;
    private final float trebleSmoothing = 0.3f;
    private final float volumeSmoothing = 0.2f;
    private FFT fft = new FFT(2048);

    private TitleAudioAnalyzer() {
    }

    public static TitleAudioAnalyzer getInstance() {
        return INSTANCE;
    }

    public void startAnalysis() {
        if (this.analyzing) {
            return;
        }
        this.analysisExecutor.submit(() -> {
            try {
                AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                try {
                    try {
                        this.audioLine = AudioSystem.getTargetDataLine(format);
                        this.audioLine.open(format, 4096);
                        this.audioLine.start();
                        MeteorClient.LOG.info("[TitleScreen] Audio analysis started");
                    } catch (Exception e) {
                        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
                        boolean foundMixer = false;
                        for (Mixer.Info mixerInfo : mixerInfos) {
                            try {
                                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                                if (mixer.isLineSupported(info)) {
                                    this.audioLine = mixer.getLine(info);
                                    this.audioLine.open(format, 4096);
                                    this.audioLine.start();
                                    MeteorClient.LOG.info("[TitleScreen] Audio analysis started with mixer: " + mixerInfo.getName());
                                    foundMixer = true;
                                    break;
                                }
                            } catch (Exception e2) {
                            }
                        }
                        if (!foundMixer) {
                            throw new LineUnavailableException("No suitable audio mixer found");
                        }
                    }
                    this.analyzing = true;
                    analyzeAudio();
                } catch (Exception e3) {
                    MeteorClient.LOG.error("[TitleScreen] Audio capture failed, using simulation: " + e3.getMessage());
                    this.analyzing = true;
                    simulateAudioAnalysis();
                }
            } catch (Exception e4) {
                MeteorClient.LOG.error("[TitleScreen] Audio analysis error", e4);
            }
        });
    }

    public void stopAnalysis() {
        this.analyzing = false;
        if (this.audioLine != null) {
            this.audioLine.stop();
            this.audioLine.close();
            this.audioLine = null;
        }
        this.bassLevel = 0.0f;
        this.trebleLevel = 0.0f;
        this.volumeLevel = 0.0f;
    }

    private void analyzeAudio() {
        byte[] buffer = new byte[4096];
        float[] samples = new float[2048];
        int samplesIdx = 0;
        while (this.analyzing && this.audioLine != null && this.audioLine.isOpen()) {
            int bytesRead = this.audioLine.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                float currentVolume = 0.0f;
                ByteBuffer bb = ByteBuffer.wrap(buffer, 0, bytesRead);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < bytesRead; i += 4) {
                    if (i + 3 < bytesRead) {
                        short left = bb.getShort(i);
                        short right = bb.getShort(i + 2);
                        float sample = (left + right) / 65536.0f;
                        float windowedSample = sample * (0.5f - (0.5f * ((float) Math.cos((6.283185307179586d * ((double) samplesIdx)) / 2047.0d))));
                        if (samplesIdx < 2048) {
                            int i2 = samplesIdx;
                            samplesIdx++;
                            samples[i2] = windowedSample;
                        }
                        currentVolume += Math.abs(sample);
                    }
                }
                if (samplesIdx >= 2048) {
                    samplesIdx = 0;
                    this.volumeLevel = (this.volumeLevel * 0.2f) + ((currentVolume / 2048.0f) * 0.8f);
                    this.fft.forward(samples);
                    float currentBass = 0.0f;
                    float currentTreble = 0.0f;
                    int bassCount = 0;
                    int trebleCount = 0;
                    for (int i3 = 1; i3 < 1024; i3++) {
                        float magnitude = this.fft.getMagnitude(i3);
                        this.spectrum[i3] = magnitude;
                        if (i3 < 11) {
                            currentBass += magnitude * 1.5f;
                            bassCount++;
                        } else if (i3 > 92) {
                            currentTreble += magnitude * 1.2f;
                            trebleCount++;
                        }
                    }
                    if (bassCount > 0) {
                        currentBass /= bassCount;
                    }
                    if (trebleCount > 0) {
                        currentTreble /= trebleCount;
                    }
                    this.bassLevel = (this.bassLevel * 0.4f) + (currentBass * 0.6f);
                    this.trebleLevel = (this.trebleLevel * 0.3f) + (currentTreble * 0.7f);
                    long currentTime = System.currentTimeMillis();
                    if (currentBass > this.bassPeak * 1.3f && currentTime - this.lastBassPeakTime > PEAK_RESET_TIME) {
                        this.bassBeat = true;
                        this.bassPeak = currentBass;
                        this.lastBassPeakTime = currentTime;
                    } else {
                        this.bassBeat = false;
                        if (currentTime - this.lastBassPeakTime > PEAK_RESET_TIME) {
                            this.bassPeak *= 0.95f;
                        }
                    }
                    if (currentTreble > this.treblePeak * 1.3f && currentTime - this.lastTreblePeakTime > PEAK_RESET_TIME) {
                        this.trebleBeat = true;
                        this.treblePeak = currentTreble;
                        this.lastTreblePeakTime = currentTime;
                    } else {
                        this.trebleBeat = false;
                        if (currentTime - this.lastTreblePeakTime > PEAK_RESET_TIME) {
                            this.treblePeak *= 0.95f;
                        }
                    }
                }
            }
            try {
                Thread.sleep(16L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void simulateAudioAnalysis() {
        long startTime = System.currentTimeMillis();
        while (this.analyzing) {
            float time = (System.currentTimeMillis() - startTime) / 1000.0f;
            float bassPulse = (float) (0.4000000059604645d + (0.6000000238418579d * Math.sin(((double) time) * 1.5d) * Math.sin(((double) time) * 0.4d)));
            if (Math.sin(((double) time) * 0.12d) > 0.95d) {
                bassPulse *= 2.5f;
                this.bassBeat = true;
            } else {
                this.bassBeat = false;
            }
            float treblePulse = ((float) (0.20000000298023224d + (0.800000011920929d * Math.sin(((double) time) * 4.0d) * Math.sin((((double) time) * 3.2d) + 0.5d)))) * (1.0f + (((float) Math.random()) * 0.2f));
            if (Math.sin(((double) time) * 0.3d) > 0.9d) {
                treblePulse *= 2.0f;
                this.trebleBeat = true;
            } else {
                this.trebleBeat = false;
            }
            this.bassLevel = (this.bassLevel * 0.4f) + (bassPulse * 0.6f);
            this.trebleLevel = (this.trebleLevel * 0.3f) + (treblePulse * 0.7f);
            this.volumeLevel = (this.bassLevel + this.trebleLevel) * 0.5f;
            for (int i = 0; i < this.spectrum.length; i++) {
                if (i < ((double) this.spectrum.length) * 0.1d) {
                    this.spectrum[i] = bassPulse * (1.0f - (i / (this.spectrum.length * 0.1f))) * (0.5f + (0.5f * ((float) Math.sin((((double) time) * 2.0d) + (((double) i) * 0.1d)))));
                } else if (i < ((double) this.spectrum.length) * 0.5d) {
                    this.spectrum[i] = 0.3f * (0.5f + (0.5f * ((float) Math.sin((((double) time) * 3.0d) + (((double) i) * 0.05d)))));
                } else {
                    this.spectrum[i] = treblePulse * (i / this.spectrum.length) * (0.5f + (0.5f * ((float) Math.sin((((double) time) * 5.0d) + (((double) i) * 0.02d)))));
                }
            }
            try {
                Thread.sleep(16L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public float getBassLevel() {
        return this.bassLevel;
    }

    public float getTrebleLevel() {
        return this.trebleLevel;
    }

    public float getVolumeLevel() {
        return this.volumeLevel;
    }

    public float[] getSpectrum() {
        return this.spectrum;
    }

    public boolean isBassBeat() {
        return this.bassBeat;
    }

    public boolean isTrebleBeat() {
        return this.trebleBeat;
    }

    public boolean isAnalyzing() {
        return this.analyzing;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/util/audio/TitleAudioAnalyzer$FFT.class */
    private static class FFT {
        private final int n;
        private final float[] real;
        private final float[] imag;

        public FFT(int n) {
            this.n = n;
            this.real = new float[n];
            this.imag = new float[n];
        }

        public void forward(float[] samples) {
            int k;
            int i = 0;
            while (i < this.n) {
                this.real[i] = i < samples.length ? samples[i] : 0.0f;
                this.imag[i] = 0.0f;
                i++;
            }
            applyWindow();
            int j = 0;
            for (int i2 = 0; i2 < this.n - 1; i2++) {
                if (i2 < j) {
                    float temp = this.real[i2];
                    this.real[i2] = this.real[j];
                    this.real[j] = temp;
                    float temp2 = this.imag[i2];
                    this.imag[i2] = this.imag[j];
                    this.imag[j] = temp2;
                }
                int i3 = this.n;
                while (true) {
                    k = i3 / 2;
                    if (k <= j) {
                        j -= k;
                        i3 = k;
                    }
                }
                j += k;
            }
            int i4 = 1;
            while (true) {
                int step = i4;
                if (step < this.n) {
                    float psr = (float) Math.cos(3.141592653589793d / ((double) step));
                    float psi = (float) (-Math.sin(3.141592653589793d / ((double) step)));
                    float csr = 1.0f;
                    float csi = 0.0f;
                    for (int group = 0; group < step; group++) {
                        int i5 = group;
                        while (true) {
                            int pair = i5;
                            if (pair < this.n) {
                                int match = pair + step;
                                float tr = (this.real[match] * csr) - (this.imag[match] * csi);
                                float ti = (this.real[match] * csi) + (this.imag[match] * csr);
                                this.real[match] = this.real[pair] - tr;
                                this.imag[match] = this.imag[pair] - ti;
                                float[] fArr = this.real;
                                fArr[pair] = fArr[pair] + tr;
                                float[] fArr2 = this.imag;
                                fArr2[pair] = fArr2[pair] + ti;
                                i5 = pair + (2 * step);
                            }
                        }
                        float next = (csr * psr) - (csi * psi);
                        csi = (csr * psi) + (csi * psr);
                        csr = next;
                    }
                    i4 = step * 2;
                } else {
                    return;
                }
            }
        }

        private void applyWindow() {
            for (int i = 0; i < this.n; i++) {
                float[] fArr = this.real;
                int i2 = i;
                fArr[i2] = fArr[i2] * 0.5f * (1.0f - ((float) Math.cos((6.283185307179586d * ((double) i)) / ((double) (this.n - 1)))));
            }
        }

        public float getMagnitude(int index) {
            if (index < 0 || index >= this.n / 2) {
                return 0.0f;
            }
            float mag = (float) Math.sqrt((this.real[index] * this.real[index]) + (this.imag[index] * this.imag[index]));
            float freq = (index * 44100.0f) / this.n;
            float fq = freq * freq;
            float weight = (fq * fq) / (((fq + 424.36002f) * (fq + 1.4884E8f)) * Math.max(1.0f, (fq + 11599.289f) * (fq + 544496.44f)));
            return mag * weight * 1000.0f;
        }
    }
}
