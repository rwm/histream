package de.sekmi.histream.impl;

import java.util.logging.Logger;


public class LoggingDemo {
	private static final Logger log = Logger.getLogger(LoggingDemo.class.getName());

	public static void main(String[] args) {
		log.finest("finest");
		log.finer("finest");
		log.fine("fine");
		log.info("info");
		log.config("config");
		log.warning("warning");
		log.severe("severe");
		System.out.println("Wrote 7 example messages to logging");
	}

}
