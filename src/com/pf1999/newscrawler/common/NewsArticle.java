package com.pf1999.newscrawler.common;

public class NewsArticle {
	public String category 	= "";
	public String id 		= "";
	public String url 		= "";
	public String title 	= "";
	public String press 	= "";
	public String date 		= "";
	public String article 	= "";
	
	public NewsArticle(String category, String id, String url, String title, String press) {
		this.category = category;
		this.id = id;
		this.url = url;
		this.title = title;
		this.press = press;
	}
	public NewsArticle(String category, String id, String url, String title, String press, String date, String article) {
		this.category = category;
		this.id = id;
		this.url = url;
		this.title = title;
		this.press = press;
		this.date = date;
		this.article = article;
	}

	public String parseLine() {
		return category + "#@," + id + "#@," + url + "#@," + title + "#@," + press + "#@," + date + "#@," + article;
	}
}
