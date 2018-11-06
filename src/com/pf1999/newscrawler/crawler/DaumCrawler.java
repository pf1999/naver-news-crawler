/*
 * 
 * 
 */

package com.pf1999.newscrawler.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
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

public class DaumCrawler extends Thread{
	
	public DaumCrawler() {
		sb = new StringBuilder();
		driver = new ChromeDriver();
		articles = new ArrayList<>();
		latestId = new HashMap<>();
	}
	
	/*
	 * Predefined constants
	 */
	private static final String URL_HOME = "https://media.daum.net/breakingnews/";
	
	private static final String SID1[] = {"society", "politics", "economic", "foreign", "culture", "entertain", "digital", "sports"};
	public static final int CAT_SOCIETY 	= 1 << 0;
	public static final int CAT_POLITICS 	= 1 << 1;
	public static final int CAT_ECONOMIC 	= 1 << 2;
	public static final int CAT_FOREIGN		= 1 << 3;
	public static final int CAT_CULTURE 	= 1 << 4;
	public static final int CAT_ENTERTAIN 	= 1 << 5;
	public static final int CAT_DIGITAL 	= 1 << 6;
	public static final int CAT_SPORTS	 	= 1 << 7;
	
	/*
	 * Crawler configuration variables
	 */
	private int interval 	= 1000 * 60 * 5; // ms
	private int category	= CAT_SOCIETY | CAT_POLITICS | CAT_ECONOMIC | CAT_FOREIGN | CAT_CULTURE | CAT_ENTERTAIN | CAT_DIGITAL | CAT_SPORTS;
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
	HashMap<String, String>latestId;
	
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
		
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		
		ArrayList<String>cats = new ArrayList<>();
		for (int i = 0; i < SID1.length; i++) {
			if ((category & (1 << i)) == (1 << i))
				cats.add(SID1[i]);
		}
		
		while (run) {
			String anHour = df.format(Calendar.getInstance().getTime());
			
			/* Get article lists(title, url, press), not contents */
			for (int i = 0; i < cats.size(); i++) {
				while(true) {
					sb.setLength(0);
					sb.append(URL_HOME).append(cats.get(i)).append("?page=").append(page);
					
					driver.get(sb.toString());
					doc = Jsoup.parse(driver.getPageSource());
					
					// Entire article lists container element is named 'section_body'
					elements = doc.select(".list_allnews li");
					
					if (elements.isEmpty())
						break;
					
					for (Element e : elements) {
						Elements a = e.select("a");
						String aid = a.attr("href").substring(a.attr("href").indexOf("v/") + 2);
						
						if (latestId.get(cats.get(i)) != null && aid.compareTo(latestId.get(cats.get(i))) == 0) {
							old = true;
							break;
						}
						
						articles.add(new NewsArticle(
								cats.get(i),
								// Parse only its 'aid' that articles primary key
								aid,
								a.attr("href"),
								a.text(),
								e.select(".info_news").first().ownText()));
						
						// Daum doesn't classify article like naver's 'is_outdated' or 'is_new', so calculate posted time and
						// current time to figure out either it is passed an hour or not.
						try {
							if (df.parse(anHour).getTime() - df.parse(e.select(".info_time").text()).getTime() > (1000 * 60 * 30)) {
								old = true;
								break;
							}
						} catch (ParseException e1) {
							e1.printStackTrace();	
						}
					}
					
					// Save latest article's aid from each category to avoid getting duplicate article  
					if (page == 1) {
						String href = elements.get(0).select("a").attr("href");
						latestId.put(cats.get(i), href.substring(href.indexOf("v/") + 2));
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
				
				Elements es = doc.select(".info_view>.txt_info");
				int idx = 0;
				if (es.size() == 1) 		idx = 0;
				else if (es.size() == 2) 	idx = 1;
				else if (es.size() == 3) 	idx = 2;
				else						continue;
				article.date = es.get(idx).text().replace("입력 ", "");
				article.date = es.get(idx).text().replace("수정 ", "");
				article.article = doc.select(".news_view").text();
				
				articles.set(i, article);
			}
			
			File f = new File("./articles2.txt");
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
