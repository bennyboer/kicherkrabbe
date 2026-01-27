package de.bennyboer.kicherkrabbe.inquiries.transformers;

import de.bennyboer.kicherkrabbe.inquiries.DateRange;
import de.bennyboer.kicherkrabbe.inquiries.api.DateRangeDTO;

public class DateRangeTransformer {

    public static DateRangeDTO toApi(DateRange dateRange) {
        var result = new DateRangeDTO();

        result.from = dateRange.getFrom();
        result.to = dateRange.getTo();

        return result;
    }

}
