package org.altfund.xchangeinterface.xchange.service.util;

import org.altfund.xchangeinterface.xchange.service.util.ExtractExceptions;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.altfund.xchangeinterface.xchange.service.exceptions.XChangeServiceException;
import org.altfund.xchangeinterface.util.JsonHelper;

@Slf4j
public class ExtractOrderBooks {

    public static ObjectNode toJson(
            MarketDataService marketDataService,
            Map<String, String> params,
            JsonHelper jh) throws XChangeServiceException {

        ObjectNode errorMap = jh.getObjectNode();
        ObjectNode json = jh.getObjectNode();
        ObjectNode innerJson;
        CurrencyPair cp = null;

        try {
            innerJson = jh.getObjectNode();
            cp = new CurrencyPair(
                    params.get("quote_currency"),
                    params.get("base_currency")
                    );
            log.debug("currency pair submitted to order book {}.", cp.toString());

            try {
                innerJson = jh.getObjectMapper().convertValue(getOrderBook(marketDataService, cp), ObjectNode.class);
                json.put(cp.toString(), innerJson);
            } catch (Exception e) {
                json.put(cp.toString(), ExtractExceptions.toJson(e, jh));
            }

        } catch (RuntimeException re) {
            log.error("Non-retryable error occurred while processing exchange {}.",
                    params.get( "exchange" ));
            errorMap.put("ERROR","Falied to retrieve contents of exchange " + params.get("exchange"));
            return errorMap;
        }
        return json;
    }

    public static OrderBook raw(
                        MarketDataService marketDataService,
                        Map<String, String> params)  throws Exception {
        OrderBook orderBook = null;
        CurrencyPair cp = null;

        try {
            cp = new CurrencyPair(
                    params.get("quote_currency"),
                    params.get("base_currency")
                    );
            log.debug("currency pair submitted to order book {}.", cp.toString());

            try {
                orderBook = getOrderBook(marketDataService, cp);
            } catch (Exception e) {
                throw e;
            }

        } catch (RuntimeException re) {
            log.error("Non-retryable error occurred while processing exchange {}.",
                    params.get( "exchange" ));
            return re;
        }
        return orderBook;
    }

    private static OrderBook getOrderBook(MarketDataService marketDataService, CurrencyPair cp) throws Exception{
        return marketDataService.getOrderBook(cp);
    }
}