
package com.example.vida.service;

import java.util.Map;

public interface DepartmentService {

    Map<String, Object> searchDepartmentsByName(String searchText, Integer companyId, int page, int size);

}