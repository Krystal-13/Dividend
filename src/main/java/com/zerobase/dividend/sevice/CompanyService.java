package com.zerobase.dividend.sevice;

import com.zerobase.dividend.exception.impl.AlreadyExistCompanyException;
import com.zerobase.dividend.exception.impl.NoCompanyException;
import com.zerobase.dividend.model.Company;
import com.zerobase.dividend.model.ScrapedResult;
import com.zerobase.dividend.persist.CompanyRepository;
import com.zerobase.dividend.persist.DividendRepository;
import com.zerobase.dividend.persist.entity.CompanyEntity;
import com.zerobase.dividend.persist.entity.DividendEntity;
import com.zerobase.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Trie trie;

    public Company save(String ticker) {

        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new AlreadyExistCompanyException();
        }

        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {

        return this.companyRepository.findAll(pageable);
    }
    private Company storeCompanyAndDividend(String ticker) {

        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);

        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        CompanyEntity companyEntity =
                this.companyRepository.save(new CompanyEntity(company));

        List<DividendEntity> dividendEntities =
                scrapedResult.getDividends()
                            .stream()
                            .map(e -> new DividendEntity(companyEntity.getId(), e))
                            .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);

        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {

        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities =
                this.companyRepository
                        .findByNameStartingWithIgnoreCase(keyword, limit);

        return companyEntities.stream()
                                .map(e -> e.getName())
                                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {

        this.trie.put(keyword, null);
    }
    public List<String> autocomplete(String keyword) {

        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream().collect(Collectors.toList());
    }
    public void deleteAutocompleteKeyword(String keyword) {

        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {

        CompanyEntity company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(NoCompanyException::new);

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName());

        return company.getName();
    }
}
