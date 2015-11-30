package io.github.test;

import com.mongodb.*;

import java.util.*;
import java.io.*;

import io.github.sqlconnection.BaseConnection;

public class MongoConnection {
	public BaseConnection connection;
	public HashSet<Review> reviews;
	public HashMap<String, Double> idf;
	public Review randomReview;
	public Review query;
	
	public MongoConnection(){
		this.connection = new BaseConnection();
		this.reviews = new HashSet<Review>();
		this.idf = new HashMap<String, Double>();
	}
	public void getCosineSimilarity() throws IOException{		
		connection.connect();
		setGlobalIDF();
		setReviews();
		compareCosSimilarityQ();
		connection.close();
	}
	
	public void setReviews(){
		int firstSix = 0;
		int random = ((int) Math.floor(Math.random() * 101)) % 6;
		int counter = 0;
		connection.setDBAndCollection("cs336", "unlabel_review");
		DBCursor nosplit = connection.showRecords();
		
		while(nosplit.hasNext() && firstSix < 6){
			DBObject nosplitDBO = nosplit.next();
			Review review = new Review((String) nosplitDBO.get("id"), (String) nosplitDBO.get("review"));
			review.setTF();
			review.setTFIDF(idf);
			reviews.add(review);
			firstSix++;
		}
		
		for(Review review : reviews){
			randomReview = review;
			if(counter == random) break;
			counter++;
		}
	}
	
	public void setQuery(){
		String text = randomReview.review;
		String[] terms = text.split("\\W+");
		query = new Review("420", terms[2] + " " + terms[3]);
		query.setTF();
		query.setTFIDF(idf);
	}
	
	public void setGlobalIDF(){
		int numReviews = 0;
		connection.setDBAndCollection("cs336", "unlabel_review");
		DBCursor nosplit = connection.showRecords();
		
		while(nosplit.hasNext()){
			DBObject nosplitDBO = nosplit.next();
			String review = (String) nosplitDBO.get("review");
			String[] terms = review.split("\\W+");
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
		}
	}
	
	public void compareCosSimilarityR(){
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
	
	public void compareCosSimilarityQ(){
		setQuery();
		double cosine;
		for(Review review : reviews){
			double totalReview = 0;
			double totalQuery = 0;
			double product = 0;
			Set<String> union = new HashSet<String>();
			union.addAll(query.tfidf.keySet());
			union.addAll(review.tfidf.keySet());
			for(String term : union){
				double reviewTFIDF;
				double queryTFIDF;
				
				if(!review.tfidf.containsKey(term)) reviewTFIDF = 0;
				else reviewTFIDF = review.tfidf.get(term);
				if(!query.tfidf.containsKey(term)) queryTFIDF = 0;
				else queryTFIDF = query.tfidf.get(term);
		
				product += queryTFIDF * reviewTFIDF;
				totalReview += reviewTFIDF * reviewTFIDF;
				totalQuery += queryTFIDF * queryTFIDF;
			}
			totalReview = Math.sqrt(totalReview);
			totalQuery = Math.sqrt(totalQuery);
			cosine = product/(totalReview * totalQuery);
			System.out.println(review.toJSON());
			System.out.println(query.toJSON());
			System.out.println(cosine);
		}
	} 
}
