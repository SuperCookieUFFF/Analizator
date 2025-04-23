package ru.analiz.programm;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TextAnalyzer {

    // Блокирующие очереди для каждого символа
    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws InterruptedException {
        // Поток для генерации текстов
        Thread textGeneratorThread = new Thread(() -> {
            try {
                for (int i = 0; i < 10000; i++) {
                    String text = generateText("abc", 100000);
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                }
                // Отправляем сигнал о завершении
                queueA.put("END");
                queueB.put("END");
                queueC.put("END");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Потоки для анализа текстов
        Thread analyzerA = new Thread(new TextAnalyzerTask(queueA, 'a'));
        Thread analyzerB = new Thread(new TextAnalyzerTask(queueB, 'b'));
        Thread analyzerC = new Thread(new TextAnalyzerTask(queueC, 'c'));

        // Запуск потоков
        textGeneratorThread.start();
        analyzerA.start();
        analyzerB.start();
        analyzerC.start();

        // Ожидание завершения потоков
        textGeneratorThread.join();
        analyzerA.join();
        analyzerB.join();
        analyzerC.join();
    }

    // Генератор текстов
    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    // Задача для анализа текстов
    static class TextAnalyzerTask implements Runnable {
        private final BlockingQueue<String> queue;
        private final char targetChar;
        private String maxText = "";
        private int maxCount = 0;

        public TextAnalyzerTask(BlockingQueue<String> queue, char targetChar) {
            this.queue = queue;
            this.targetChar = targetChar;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String text = queue.take();
                    if ("END".equals(text)) {
                        break;
                    }
                    int count = countChar(text, targetChar);
                    if (count > maxCount) {
                        maxCount = count;
                        maxText = text;
                    }
                }
                System.out.println("Max count of '" + targetChar + "': " + maxCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private int countChar(String text, char targetChar) {
            int count = 0;
            for (char c : text.toCharArray()) {
                if (c == targetChar) {
                    count++;
                }
            }
            return count;
        }
    }
}