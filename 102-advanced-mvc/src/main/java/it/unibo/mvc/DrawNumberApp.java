package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
/**
 * This class implements the application controller.
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String FILE_NAME = "config.yml";
    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) throws FileNotFoundException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        /*
         * Read from file
         */
        final var builder = new Configuration.Builder();
        try (final var in = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(FILE_NAME)))) {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                final var lineRead = line.split(":");
                if (lineRead.length == 2) {
                    var key = lineRead[0].trim();
                    var value = Integer.parseInt(lineRead[1].trim());
                    if (key.contains("min")) {
                        builder.setMin(value);
                    } else if (key.contains("max")) {
                        builder.setMax(value);
                    } else if (key.contains("attempts")) {
                        builder.setAttempts(value);
                    }
                }
            }
        } catch(final IOException | NumberFormatException e) { 
            System.err.println(e.getMessage());
        }
        this.model = new DrawNumberImpl(builder.build());
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
        new DrawNumberApp(
            new DrawNumberViewImpl(),
            new DrawNumberViewImpl(),  
            new PrintStreamView(System.out),
            new PrintStreamView("logfile.log")
        ); 
        System.out.println();
    }
}
