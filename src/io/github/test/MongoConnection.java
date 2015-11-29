package io.github.test;

import com.mongodb.*;

import java.util.*;
import java.io.*;

import io.github.sqlconnection.BaseConnection;

public class MongoConnection {
	public BaseConnection connection;
	public HashSet<Review> reviews;
	public Review randomReview;
	public HashMap<String, Double> idf;
	
	public MongoConnection(){
		this.connection = new BaseConnection();
		this.reviews = new HashSet<Review>();
		this.idf = new HashMap<String, Double>();
	}
	public void getCosineSimilarity() throws IOException{		
		connection.connect();
		getReviews();
		getIDF();
		getTFIDF();
		compareCosSimilarity();
		connection.close();
	}
	
	public void getReviews(){
		int firstSix = 0;
		String[] terms;
		connection.setDBAndCollection("cs336", "unlabel_review");
		DBCursor nosplit = connection.showRecords();
		
		while(nosplit.hasNext() && firstSix < 6){
			DBObject nosplitDBO = nosplit.next();
			Review review = new Review((String) nosplitDBO.get("id"), (String) nosplitDBO.get("review"));
			terms = review.review.split("\\W+");
			for(String term : terms){
				term = term.toLowerCase();
				if(!review.tf.containsKey(term)){
					review.tf.put(term, 1.0);
				}
				else{
					review.tf.put(term, 1 + review.tf.get(term));
				}
			}
			for(String term : review.tf.keySet()){
				review.tf.put(term, 1 + Math.log10(review.tf.get(term)));
			}
			reviews.add(review);
			firstSix++;
		}
		
		for(Review review : reviews){
			randomReview = review;
			break;
		}
	}
	
	public void getIDF(){
		int numReviews = 0;
		String[] terms;
		connection.setDBAndCollection("cs336", "unlabel_review");
		DBCursor nosplit = connection.showRecords();
		
		while(nosplit.hasNext()){
			DBObject nosplitDBO = nosplit.next();
			String review = (String) nosplitDBO.get("review");
			terms = review.split("\\W+");
			for(String term : terms){
				term = term.toLowerCase();
				if(!idf.containsKey(term)){
					idf.put(term, 1.0);
				}
				else{
					idf.put(term, 1 + idf.get(term));
				}
			}
			numReviews++;
		}
		
		for(String term : idf.keySet()){
			double idfVal = Math.log10(numReviews/idf.get(term));
			idf.put(term, idfVal);
			//if(idfVal < 0) System.out.println(term + ":" + idfVal);
		}
	}
	
	public void getTFIDF(){
		for(Review review : reviews){
			for(String term : review.tf.keySet()){
				review.tfidf.put(term, review.tf.get(term) * idf.get(term));
				//System.out.println(term + ":" + review.tf.get(term) + " * " + idf.get(term));
			}
			
		}
	}
	
	public void compareCosSimilarity(){
		double cosine;
		for(Review review : reviews){
			double totalReview = 0;
			double totalRandomReview = 0;
			double product = 0;
			Set<String> union = new HashSet<String>();
			union.addAll(randomReview.tfidf.keySet());
			union.addAll(review.tfidf.keySet());
			for(String term : union){
				double reviewTFIDF;
				double randomReviewTFIDF;
				
				if(!review.tfidf.containsKey(term)) reviewTFIDF = 0;
				else reviewTFIDF = review.tfidf.get(term);
				if(!randomReview.tfidf.containsKey(term)) randomReviewTFIDF = 0;
				else randomReviewTFIDF = randomReview.tfidf.get(term);
		
				//System.out.println(reviewTFIDF + " vs " + randomReviewTFIDF);
				product += randomReviewTFIDF * reviewTFIDF;
				totalReview += reviewTFIDF * reviewTFIDF;
				totalRandomReview += randomReviewTFIDF * randomReviewTFIDF;
			}
			totalReview = Math.sqrt(totalReview);
			totalRandomReview = Math.sqrt(totalRandomReview);
			cosine = product/(totalReview * totalRandomReview);
			System.out.println(review.toJSON());
			System.out.println(randomReview.toJSON());
			System.out.println(cosine);
		}
	}
	
	/*
	public void writeReviews() throws IOException {
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Reviews.json"), "utf-8"));
		    for(Review review : reviews.keySet()){
				writer.write(review.toJSON() + "\n");
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
		finally{
			if(writer != null) writer.close();
		}
	} */
}
