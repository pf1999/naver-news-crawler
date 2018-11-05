package com.pf1999.newscrawler;

import com.pf1999.newscrawler.crawler.NaverCrawler;

public class TestMain {
	public static void main(String args[]) {
		NaverCrawler nc = new NaverCrawler();
		
		nc.start();
	}
}
