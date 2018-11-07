## News Crawler
Naver and Daum news web crawler via Jsoup + Selenium.

It will crawling all of news from naver and daum, or if you specified categories what you want, it only crawling those things.

Unfortunately naver using `ajax` to refresh page for updated news in every some minutes. So, I had to use `selenium`, because using `ajax` means web page is loaded dynamically and Jsoup cannot read them. For these behind story, you have to install `firefox` browser and download its driver. Crawler will open new instance of browser and use it to crawling.

## Core Library Versions
* [Jsoup](https://jsoup.org/download) : 1.11.3

* [Selenium Standalone](https://www.seleniumhq.org/download/) : 3.141.0

## Prerequisite
1. You have to install Firefox web browser.
1. ...and Firefox Driver too, from [here](https://github.com/mozilla/geckodriver/releases).

## How to
1. Download above core libraries from refereced link and this repository.
1. Move all of jar files of core libraries to repository directory.
1. Import project to eclipse photon or just use NaverCrawler or DaumCrawler Java file.

## Supported Categories and Specifying
* Naver : _`Breaking`_, _`Politics`_, _`Economic`_, _`Society`_, _`Culture`_, _`World`_, _`Science`_
* Daum  : _`Politics`_, _`Economic`_, _`Society`_, _`Culture`_, _`Foreign(=World)`_, _`Digital(=Science)`_, _`Sports`_, _`Entertain`_

If you want to specify each one's category, follow below code.

    public static void main(String args[]){}
      NaverCrawler ncrawler = new NaverCrawler();
      ncrawler.setCategory(NaverCrawler.CAT_CULTURE | NaverCrawler.CAT_SOCIETY | NaverCrawler.CAT_SCIENCE);  
      ncrawler.run();
    }

## Result
![today politics](https://user-images.githubusercontent.com/44758316/48113241-5e061580-e29d-11e8-89eb-179367ce673f.PNG)
