package com.increff.pos.api;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.entity.InvoicePojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.DaySalesModel;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;


@Service
@Transactional
public class InvoiceApi {

    @Autowired
    private InvoiceDao invoiceDao;

    public InvoicePojo createInvoice(InvoicePojo invoice) {
        invoiceDao.insert(invoice);
        return invoice;
    }

    public DaySalesModel getInvoicesDataByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<InvoicePojo> invoices = invoiceDao.selectByDateRange(startDate, endDate);
        int invoicedOrdersCount = invoices.size();
        int invoicedItemsCount = invoices.stream()
                .mapToInt(InvoicePojo::getCountOfItems)
                .sum();
        double totalRevenue = invoices.stream()
                .mapToDouble(InvoicePojo::getFinalRevenue)
                .sum();
        return new DaySalesModel(invoicedOrdersCount, invoicedItemsCount, totalRevenue);
    }

    public boolean existsByOrderId(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId) != null;
    }

    public InvoicePojo getInvoiceByOrderId(Integer orderId) {
        InvoicePojo invoice = invoiceDao.selectByOrderId(orderId);
        if (invoice == null) {
            throw new ApiException(ErrorType.NOT_FOUND, "Invoice not found for order: " + orderId);
        }
        return invoice;
    }

    public String getInvoicePathByOrderId(Integer orderId) {
        InvoicePojo invoice = getInvoiceByOrderId(orderId);
        return invoice.getInvoicePath();
    }
} 