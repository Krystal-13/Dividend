package com.zerobase.dividend.scraper;

import com.zerobase.dividend.exception.impl.NoTickerException;
import com.zerobase.dividend.model.constants.Month;
import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.Dividend;
import com.zerobase.dividend.model.ScrapedResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class YahooFinanceScraper implements Scraper{

    private static final String STATICS_URL =
            "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL =
            "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400;

    @Override
    public ScrapedResult scrap(Company company) {

        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;

            String url = String.format(STATICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue(
                                                "data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0);
            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException(
                            "Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(
                        LocalDateTime.of(year, month, day, 0, 0), dividend));

            }
            scrapResult.setDividends(dividends);
        } catch (IOException e) {
            log.debug("배당금 정보 가져오기에 실패했습니다.");
            e.printStackTrace();
        }
        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {

        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);
            System.out.println(titleEle);
            String title = titleEle.text().split(" \\(")[0].trim();

            return new Company(ticker, title);
        } catch (Exception e) {
            throw new NoTickerException();
        }

    }
}
