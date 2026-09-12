package cn.iocoder.yudao.server.quote;

import static cn.iocoder.yudao.server.quote.QuoteTypes.*;

/** 数据接入口。接真实成本表或新 API 时实现这个接口，计算单元不需要改动。 */
public interface QuoteDataProvider {
    Catalog catalog();
}
