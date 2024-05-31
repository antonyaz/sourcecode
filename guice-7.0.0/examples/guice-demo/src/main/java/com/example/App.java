package com.example;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Hello world, Guice! */
public class App {
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @interface Message {}

  private Printer printer;
  private String message;

    App(){};

  @Inject
  App(Printer printer, @Message String message) {
    this.printer = printer;
    this.message = message;
  }

  public void run() {
    printer.printMessage(message);
  }

  public static void main(String[] args) {
    Printer consolePrinter =
        new Printer() {
          @Override
          public void printMessage(String message) {
            System.out.println(message);
          }
        };
    App app =
        Guice.createInjector(
                new MessageModule(),
                new AbstractModule() {
                  @Override
                  protected void configure() {
                    bind(Printer.class).toInstance(consolePrinter);
                  }
                })
            .getInstance(App.class);
    app.run();
  }

  static class MessageModule extends AbstractModule {
    @Provides
    @Message
    String provideMessage() {
        return "Hello, Guice!";
    }
  }
}
