/*
 * 
 * 
 */

package com.pf1999.newscrawler.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.pf1999.newscrawler.common.NewsArticle;

public class NaverCrawler extends Thread{
	
	public NaverCrawler() {
		sb = new StringBuilder();
		driver = new ChromeDriver();
		articles = new ArrayList<>();
	}
	
	/*
	 * Predefined constants
	 */
	private static final String URL_HOME = "https://news.naver.com";
	private static final String URL_HEADLINE = "/main/main.nhn?mode=LSS3D&mid=shm&";
	
	private static final int sid1[] = {100, 101, 102, 103, 104, 105};
	private static final int CAT_POLITICS 	= 1 << 0;
	private static final int CAT_ECONOMY 	= 1 << 1;
	private static final int CAT_SOCIETY 	= 1 << 2;
	private static final int CAT_CULTURE 	= 1 << 3;
	private static final int CAT_WORLD 		= 1 << 4;
	private static final int CAT_SCIENCE 	= 1 << 5;
	
	/*
	 * Crawler configuration variables
	 */
	private int interval 	= 1000 * 60 * 30; // ms
	private int category	= CAT_POLITICS | CAT_ECONOMY | CAT_SOCIETY | CAT_CULTURE | CAT_WORLD | CAT_SCIENCE;
	private boolean run		= true;
	private StringBuilder sb;
	
	/*
	 * Crawler configuration getter methods
	 */
	public int getInterval() { return interval; }
	public int getCategory() { return category; }
	
	/*
	 * Crawler configuration setter methods
	 */
	public void setInterval(int interval) { this.interval = interval; }
	public void setCategory(int category) { this.category = category; }
	
	/*
	 * Crawler variables
	 */
	WebDriver driver;
	ArrayList<NewsArticle> articles;
	
	/*
	 * Crawler mothods
	 */
	public void close() { this.run = false; }
	
	@Override
	public void run() {
		Connection.Response res = null;
		Document doc = null;
		Elements elements = null;
		
		int page = 1;
		boolean old = false;
		
//		while (run) {
			// Get article lists(title, url, press), not contents
			for (int i = 0; i < sid1.length; i++) {
				while(true) {
					sb.setLength(0);
					sb.append(URL_HOME).append(URL_HEADLINE).append("sid1=").append(sid1[i]).append("#&date=%2000:00:00&page=").append(page);
					
					driver.get(sb.toString());
					doc = Jsoup.parse(driver.getPageSource());
					
					elements = doc.select("#section_body li");
					if (elements.isEmpty())
						break;
					
					for (Element e : elements) {
						Elements a = e.select("a");
						articles.add(new NewsArticle(
								sid1[i],
								a.attr("href").substring(a.attr("href").indexOf("aid=") + 4),
								a.attr("href"),
								a.text(),
								e.select(".writing").text()));
						
						if (e.select(".is_new").isEmpty()) {
							old = true;
							break;
						}
					}
					
					if (old) break;
					++page;
				}
				old = false;
				page = 1;
			}
			
			// Get articles' content(date, article)
			for (int i = 0; i < articles.size(); i++) {
				NewsArticle article = articles.get(i);
				
				sb.setLength(0);
				sb.append(URL_HOME).append(article.url);
				
				driver.get(sb.toString());
				doc = Jsoup.parse(driver.getPageSource());
				
				article.date = doc.select(".t11").first().text();
				article.article = doc.select("#articleBodyContents").text();
				
				articles.set(i, article);
			}
			
			File f = new File("D:/articles.txt");
			FileWriter fw = null;
			try {
				fw = new FileWriter(f, true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			for (NewsArticle a : articles) {
				try {
					fw.write(a.parseLine() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
//			try {
//				Thread.sleep(interval);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				close();
//			}
//		}
	}
}
