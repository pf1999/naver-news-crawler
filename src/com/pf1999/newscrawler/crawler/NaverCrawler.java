/*
 * 
 * 
 */

package com.pf1999.newscrawler.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

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
		latestId = new HashMap<>();
	}
	
	/*
	 * Predefined constants
	 */
	private static final String URL_HOME = "https://news.naver.com";
	private static final String URL_HEADLINE = "/main/main.nhn?mode=LSD&mid=shm&";
	private static final String URL_BREAKING = "/main/list.nhn?mode=LSD&mid=sec&";
	
	private static final int SID1[] = {001, 100, 101, 102, 103, 104, 105};
	public static final int CAT_BREAKING	= 1 << 0;
	public static final int CAT_POLITICS 	= 1 << 1;
	public static final int CAT_ECONOMY 	= 1 << 2;
	public static final int CAT_SOCIETY 	= 1 << 3;
	public static final int CAT_CULTURE 	= 1 << 4;
	public static final int CAT_WORLD 		= 1 << 5;
	public static final int CAT_SCIENCE 	= 1 << 6;
	
	/*
	 * Crawler configuration variables
	 */
	private int interval 	= 1000 * 60 * 5; // ms
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
	HashMap<Integer, String>latestId;
	
	/*
	 * Crawler mothods
	 */
	public void close() {
		this.run = false;
		driver.close();
	}
	
	@Override
	public void run() {
		Document doc = null;
		Elements elements = null;
		
		int page = 1;
		boolean old = false;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		
		ArrayList<Integer>cats = new ArrayList<>();
		for (int i = 0; i < SID1.length; i++) {
			if ((category & (1 << i)) == (1 << i))
				cats.add(SID1[i]);
		}
		
		while (run) {
			/* Get article lists(title, url, press), not contents */
			for (int i = 0; i < cats.size(); i++) {
				while(true) {
					sb.setLength(0);
					if (cats.get(i) == 1) {
						sb.append(URL_HOME).append(URL_BREAKING)
							.append("sid1=001")
							.append("&date=").append(df.format(Calendar.getInstance().getTime()))
							.append("&page=").append(page);
					}
					else {
						sb.append(URL_HOME).append(URL_HEADLINE)
							.append("sid1=").append(cats.get(i))
							.append("#&date=%2000:00:00&page=").append(page);
					}
					
					driver.get(sb.toString());
					doc = Jsoup.parse(driver.getPageSource());
					
					// Entire article lists container element is named 'section_body'
					if (cats.get(i) == 1)
						elements = doc.select("#main_content li");
					else
						elements = doc.select("#section_body li");
					if (elements.isEmpty())
						break;
					
					for (Element e : elements) {
						Elements a = e.select("a");
						String aid = a.attr("href").substring(a.attr("href").indexOf("aid=") + 4);
						
						if (latestId.get(cats.get(i)) != null && aid.compareTo(latestId.get(cats.get(i))) == 0) {
							old = true;
							break;
						}
						
						String url = "";
						if (cats.get(i) == 1)
							url = a.attr("href");
						else
							url = URL_HOME + a.attr("href");
						
						articles.add(new NewsArticle(
								cats.get(i),
								// Parse only its 'aid' that articles primary key
								aid,
								url,
								a.text(),
								e.select(".writing").text()));
						
						// Naver classify article 'outdated' after an hour since published. As this policy, only collect 'new' articles
						if (e.select(".is_new").isEmpty()) {
							old = true;
							break;
						}
					}
					
					// Save latest article's aid from each category to avoid getting duplicate article  
					if (page == 1) {
						String href = elements.get(0).select("a").attr("href");
						latestId.put(cats.get(i), href.substring(href.indexOf("aid=") + 4));
					}
					
					if (old) break;
					++page;
				}
				old = false;
				page = 1;
			}
			
			/* Get articles' content(date, article) */
			for (int i = 0; i < articles.size(); i++) {
				NewsArticle article = articles.get(i);
				
				sb.setLength(0);
				sb.append(article.url);
				
				driver.get(sb.toString());
				doc = Jsoup.parse(driver.getPageSource());
				
				article.date = doc.select(".t11").first().text();
				article.article = doc.select("#articleBodyContents").text();
				
				articles.set(i, article);
			}
			
			File f = new File("./articles.txt");
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
			
			articles.clear();
			
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
				close();
			}
		}
	}
}
