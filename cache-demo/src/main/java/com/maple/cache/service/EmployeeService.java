package com.maple.cache.service;

import com.maple.cache.entry.Employee;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author 陈其丰
 */
@CacheConfig(cacheNames = "emp")
@Service
public class EmployeeService {

    @Cacheable(key = "#id")
    public Employee getById(Integer id){
        System.out.println("method: getById");
        return this.findById(id);
    }

    @CachePut(key = "#id")
    public Employee updateById(Integer id, String name){
        System.out.println("method: updateById");
        Employee employee = this.findById(id);
        employee.setName(name);
        return employee;
    }

    @CacheEvict( key = "#id")
    public void deleteById(Integer id){
        System.out.println("delete id: " + id);
    }


    public Employee findById(Integer id){
        Employee[] array = new Employee[]{
                new Employee(1,"a"),
                new Employee(2,"b"),
                new Employee(3,"c"),
                new Employee(4,"d"),
                new Employee(5,"e"),
        };
        for (Employee employee : array) {
            if(employee.getId().equals(id)){
                return employee;
            }
        }
        return null;
    };
}
