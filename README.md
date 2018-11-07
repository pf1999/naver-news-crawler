## News-Crawler
Naver and Daum news web crawler via JSoup + Selenium.

## Core Library Versions
* [Jsoup](https://jsoup.org/download) : 1.11.3

* [Selenium Standalone](https://www.seleniumhq.org/download/) : 3.141.0

## Prerequisite
1. You have to install chrome(it will be change to firefox further) web browser.

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
