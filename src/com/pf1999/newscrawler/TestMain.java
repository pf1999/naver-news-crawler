package com.pf1999.newscrawler;

import java.util.ArrayList;

import com.pf1999.newscrawler.crawler.DaumCrawler;
import com.pf1999.newscrawler.crawler.NaverCrawler;

public class TestMain {
	public static void main(String args[]) {
		ArrayList<Thread> list = new ArrayList<>();
		
		// list.add(new NaverCrawler());
		list.add(new DaumCrawler());
		
		for (Thread t : list) {
			t.start();
		}
		
		for (Thread t : list) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
