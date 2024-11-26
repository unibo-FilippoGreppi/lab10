package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;
    private static final String FILE_NAME = "resources" + File.separator + "config.yml";
    private  int max; 
    private  int min;
    private  int attempts;
    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        readFromFile();
        this.model = new DrawNumberImpl(min, max, attempts);
    }

    private void readFromFile() {
        try (final BufferedReader in = new BufferedReader(new FileReader(FILE_NAME))) {
            final String[] firstLine = in.readLine().split(":");
            final String[] secondLine = in.readLine().split(":");
            final String[] thirdLine = in.readLine().split(":");
            this.min = Integer.parseInt(firstLine[firstLine.length - 1]);
            this.max = Integer.parseInt(secondLine[secondLine.length - 1]);
            this.attempts = Integer.parseInt(thirdLine[thirdLine.length - 1]);
        } catch(IOException e) {
            this.min = MIN;
            this.max = MAX;
            this.attempts = ATTEMPTS;
        }
    }
    
    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl()); 
    }

}
