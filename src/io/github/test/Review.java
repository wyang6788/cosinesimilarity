package io.github.test;

import java.util.HashMap;

public class Review {
	
	public String id;
	public String review;
	public HashMap<String, Double> tf;
	public HashMap<String, Double> tfidf;
	
	public Review(String id, String review) {
		this.id = id;
		this.review = review;
		this.tf = new HashMap<String, Double>();
		this.tfidf = new HashMap<String, Double>();
	}
	
	public void printTF(){
		for(String term : tf.keySet()){
			System.out.println(term + ":" + tf.get(term));
		}
	}
	
	public void printTFIDF(){
		for(String term : tfidf.keySet()){
			System.out.println(term + ":" + tfidf.get(term));
		}
	}
	
	public String toJSON() {
		return "{id: " + id + ", review: " + review + "}";
	}
}
