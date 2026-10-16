package com.acme.retail.customer;

import com.acme.retail.common.StringUtil;
import java.util.List;

public class CustomerService {

    private static CustomerService instance;
    private CustomerDao dao = new CustomerDao();

    public static synchronized CustomerService getInstance() {
        if (instance == null) {
            instance = new CustomerService();
        }
        return instance;
    }

    public CustomerVO register(String name, String email, String phone, double creditLimit) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("Name is required");
        }
        if (StringUtil.isEmpty(email) || email.indexOf('@') < 1) {
            throw new IllegalArgumentException("A valid email is required");
        }
        if (dao.findByEmail(email) != null) {
            throw new IllegalStateException("Email already registered: " + email);
        }
        CustomerVO c = new CustomerVO();
        c.setName(name.trim());
        c.setEmail(email.trim().toLowerCase());
        c.setPhone(phone);
        c.setStatus("ACTIVE");
        c.setCreditLimit(creditLimit > 0 ? creditLimit : 10000.00);   // default credit limit
        long id = dao.insert(c);
        if (id < 0) {
            throw new RuntimeException("Could not create customer");
        }
        c.setId(id);
        System.out.println("Customer registered: " + c.getEmail());
        return c;
    }

    public CustomerVO get(long id) {
        return dao.findById(id);
    }

    /** Customer must exist and be ACTIVE (used by order placement). */
    public CustomerVO getActive(long id) {
        CustomerVO c = dao.findById(id);
        if (c == null) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        if (!"ACTIVE".equals(c.getStatus())) {
            throw new IllegalStateException("Customer is not active: " + id);
        }
        return c;
    }

    public List<CustomerVO> search(String name) {
        return dao.searchByName(name == null ? "" : name);
    }

    public boolean suspend(long id) {
        return dao.updateStatus(id, "SUSPENDED");
    }

    public boolean activate(long id) {
        return dao.updateStatus(id, "ACTIVE");
    }
}
