package meteordevelopment.meteorclient.util.audio;

import meteordevelopment.meteorclient.MeteorClient;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TitleAudioAnalyzer {
    private static final TitleAudioAnalyzer INSTANCE = new TitleAudioAnalyzer();

    private static final int BUFFER_SIZE = 4096;
    private static final int SAMPLE_RATE = 44100;
    private static final int FFT_SIZE = 2048;
    private static final int BASS_MAX_FREQ = 250;
    private static final int MID_MAX_FREQ = 2000;

    private float bassLevel = 0.0f;
    private float trebleLevel = 0.0f;
    private float volumeLevel = 0.0f;
    private float[] spectrum = new float[FFT_SIZE / 2];

    private float bassPeak = 0.0f;
    private float treblePeak = 0.0f;
    private long lastBassPeakTime = 0;
    private long lastTreblePeakTime = 0;
    private static final long PEAK_RESET_TIME = 300;
    private boolean bassBeat = false;
    private boolean trebleBeat = false;

    private boolean analyzing = false;
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private TargetDataLine audioLine;
    private FFT fft;

    private final float bassSmoothing = 0.4f;
    private final float trebleSmoothing = 0.3f;
    private final float volumeSmoothing = 0.2f;

    private TitleAudioAnalyzer() {
        fft = new FFT(FFT_SIZE);
    }

    public static TitleAudioAnalyzer getInstance() {
        return INSTANCE;
    }

    public void startAnalysis() {
        if (analyzing) return;

        analysisExecutor.submit(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                try {
                    try {
                        audioLine = AudioSystem.getTargetDataLine(format);
                        audioLine.open(format, BUFFER_SIZE);
                        audioLine.start();
                        MeteorClient.LOG.info("[TitleScreen] Audio analysis started");
                    } catch (Exception e) {
                        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
                        boolean foundMixer = false;
                        for (Mixer.Info mixerInfo : mixerInfos) {
                            try {
                                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                                if (mixer.isLineSupported(info)) {
                                    audioLine = (TargetDataLine) mixer.getLine(info);
                                    audioLine.open(format, BUFFER_SIZE);
                                    audioLine.start();
                                    MeteorClient.LOG.info("[TitleScreen] Audio analysis started with mixer: " + mixerInfo.getName());
                                    foundMixer = true;
                                    break;
                                }
                            } catch (Exception ignored) {}
                        }
                        if (!foundMixer) throw new LineUnavailableException("No suitable audio mixer found");
                    }

                    analyzing = true;
                    analyzeAudio();
                } catch (Exception e) {
                    MeteorClient.LOG.error("[TitleScreen] Audio capture failed, using simulation: " + e.getMessage());
                    analyzing = true;
                    simulateAudioAnalysis();
                }
            } catch (Exception e) {
                MeteorClient.LOG.error("[TitleScreen] Audio analysis error", e);
            }
        });
    }

    public void stopAnalysis() {
        analyzing = false;
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
            audioLine = null;
        }
        bassLevel = 0;
        trebleLevel = 0;
        volumeLevel = 0;
    }

    private void analyzeAudio() {
        byte[] buffer = new byte[BUFFER_SIZE];
        float[] samples = new float[FFT_SIZE];
        int samplesIdx = 0;

        while (analyzing) {
            if (audioLine == null || !audioLine.isOpen()) break;

            int bytesRead = audioLine.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                float currentVolume = 0;
                ByteBuffer bb = ByteBuffer.wrap(buffer, 0, bytesRead);
                bb.order(ByteOrder.LITTLE_ENDIAN);

                for (int i = 0; i < bytesRead; i += 4) {
                    if (i + 3 < bytesRead) {
                        short left = bb.getShort(i);
                        short right = bb.getShort(i + 2);
                        float sample = (left + right) / 65536.0f;
                        float windowedSample = sample * (0.5f - 0.5f * (float) Math.cos(2 * Math.PI * samplesIdx / (FFT_SIZE - 1)));
                        if (samplesIdx < FFT_SIZE) samples[samplesIdx++] = windowedSample;
                        currentVolume += Math.abs(sample);
                    }
                }

                if (samplesIdx >= FFT_SIZE) {
                    samplesIdx = 0;
                    currentVolume /= FFT_SIZE;
                    volumeLevel = volumeLevel * volumeSmoothing + currentVolume * (1 - volumeSmoothing);

                    fft.forward(samples);

                    float currentBass = 0, currentTreble = 0;
                    int bassCount = 0, trebleCount = 0;
                    int bassMaxBin = (int) ((BASS_MAX_FREQ * FFT_SIZE) / SAMPLE_RATE);
                    int midMaxBin = (int) ((MID_MAX_FREQ * FFT_SIZE) / SAMPLE_RATE);

                    for (int i = 1; i < FFT_SIZE / 2; i++) {
                        float magnitude = fft.getMagnitude(i);
                        spectrum[i] = magnitude;
                        if (i < bassMaxBin) { currentBass += magnitude * 1.5f; bassCount++; }
                        else if (i > midMaxBin) { currentTreble += magnitude * 1.2f; trebleCount++; }
                    }

                    if (bassCount > 0) currentBass /= bassCount;
                    if (trebleCount > 0) currentTreble /= trebleCount;

                    bassLevel = bassLevel * bassSmoothing + currentBass * (1 - bassSmoothing);
                    trebleLevel = trebleLevel * trebleSmoothing + currentTreble * (1 - trebleSmoothing);

                    long currentTime = System.currentTimeMillis();
                    if (currentBass > bassPeak * 1.3f && currentTime - lastBassPeakTime > PEAK_RESET_TIME) {
                        bassBeat = true; bassPeak = currentBass; lastBassPeakTime = currentTime;
                    } else {
                        bassBeat = false;
                        if (currentTime - lastBassPeakTime > PEAK_RESET_TIME) bassPeak *= 0.95f;
                    }
                    if (currentTreble > treblePeak * 1.3f && currentTime - lastTreblePeakTime > PEAK_RESET_TIME) {
                        trebleBeat = true; treblePeak = currentTreble; lastTreblePeakTime = currentTime;
                    } else {
                        trebleBeat = false;
                        if (currentTime - lastTreblePeakTime > PEAK_RESET_TIME) treblePeak *= 0.95f;
                    }
                }
            }

            try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    private void simulateAudioAnalysis() {
        long startTime = System.currentTimeMillis();

        while (analyzing) {
            float time = (System.currentTimeMillis() - startTime) / 1000.0f;

            float bassPulse = (float) (0.4f + 0.6f * Math.sin(time * 1.5) * Math.sin(time * 0.4));
            if (Math.sin(time * 0.12) > 0.95) { bassPulse *= 2.5f; bassBeat = true; }
            else bassBeat = false;

            float treblePulse = (float) (0.2f + 0.8f * Math.sin(time * 4.0) * Math.sin(time * 3.2 + 0.5));
            treblePulse *= (1.0f + (float) Math.random() * 0.2f);
            if (Math.sin(time * 0.3) > 0.9) { treblePulse *= 2.0f; trebleBeat = true; }
            else trebleBeat = false;

            bassLevel = bassLevel * bassSmoothing + bassPulse * (1 - bassSmoothing);
            trebleLevel = trebleLevel * trebleSmoothing + treblePulse * (1 - trebleSmoothing);
            volumeLevel = (bassLevel + trebleLevel) * 0.5f;

            for (int i = 0; i < spectrum.length; i++) {
                if (i < spectrum.length * 0.1) {
                    spectrum[i] = bassPulse * (1.0f - (i / (spectrum.length * 0.1f))) * (0.5f + 0.5f * (float) Math.sin(time * 2.0 + i * 0.1));
                } else if (i < spectrum.length * 0.5) {
                    spectrum[i] = 0.3f * (0.5f + 0.5f * (float) Math.sin(time * 3.0 + i * 0.05));
                } else {
                    spectrum[i] = treblePulse * (i / (float) spectrum.length) * (0.5f + 0.5f * (float) Math.sin(time * 5.0 + i * 0.02));
                }
            }

            try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }
    }

    public float getBassLevel() { return bassLevel; }
    public float getTrebleLevel() { return trebleLevel; }
    public float getVolumeLevel() { return volumeLevel; }
    public float[] getSpectrum() { return spectrum; }
    public boolean isBassBeat() { return bassBeat; }
    public boolean isTrebleBeat() { return trebleBeat; }
    public boolean isAnalyzing() { return analyzing; }

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
            for (int i = 0; i < n; i++) {
                real[i] = (i < samples.length) ? samples[i] : 0;
                imag[i] = 0;
            }
            applyWindow();

            int j = 0;
            for (int i = 0; i < n - 1; i++) {
                if (i < j) {
                    float temp = real[i]; real[i] = real[j]; real[j] = temp;
                    temp = imag[i]; imag[i] = imag[j]; imag[j] = temp;
                }
                int k = n / 2;
                while (k <= j) { j -= k; k /= 2; }
                j += k;
            }

            for (int step = 1; step < n; step *= 2) {
                float psr = (float) Math.cos(Math.PI / step);
                float psi = (float) -Math.sin(Math.PI / step);
                float csr = 1, csi = 0;
                for (int group = 0; group < step; group++) {
                    for (int pair = group; pair < n; pair += 2 * step) {
                        int match = pair + step;
                        float tr = real[match] * csr - imag[match] * csi;
                        float ti = real[match] * csi + imag[match] * csr;
                        real[match] = real[pair] - tr;
                        imag[match] = imag[pair] - ti;
                        real[pair] += tr;
                        imag[pair] += ti;
                    }
                    float next = csr * psr - csi * psi;
                    csi = csr * psi + csi * psr;
                    csr = next;
                }
            }
        }

        private void applyWindow() {
            for (int i = 0; i < n; i++) {
                real[i] *= 0.5f * (1 - (float) Math.cos(2 * Math.PI * i / (n - 1)));
            }
        }

        public float getMagnitude(int index) {
            if (index < 0 || index >= n / 2) return 0;
            float mag = (float) Math.sqrt(real[index] * real[index] + imag[index] * imag[index]);
            float freq = index * 44100f / n;
            float fq = freq * freq;
            float weight = fq * fq / ((fq + 20.6f * 20.6f) * (fq + 12200f * 12200f) *
                Math.max(1, (fq + 107.7f * 107.7f) * (fq + 737.9f * 737.9f)));
            return mag * weight * 1000f;
        }
    }
}
