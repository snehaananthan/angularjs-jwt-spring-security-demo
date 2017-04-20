package com.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.main.entity.Authority;

@Repository("authorityRepository")
public interface AuthorityRepository extends JpaRepository<Authority, Integer>{
	Authority findByAuthority(String authority);

}