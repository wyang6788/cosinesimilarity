package io.github.test;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		MongoConnection mc = new MongoConnection();
		mc.getCosineSimilarity();
	}

}
