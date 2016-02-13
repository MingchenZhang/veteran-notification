/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newpackage.contentCrawler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import veteranNews.main.SingletonManager;

/**
 *
 * @author zmc94
 */
public class CrawlerManager {
	private SingletonManager sm;
	
	public CrawlerManager(SingletonManager sm){
		this.sm = sm;
	}
	
	public void scheduleCrawler(int minuteInterval){
		Crawler crawler = new Crawler(sm);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(crawler, minuteInterval, minuteInterval, TimeUnit.MINUTES);
	}
}
