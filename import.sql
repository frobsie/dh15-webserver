-- phpMyAdmin SQL Dump
-- version 4.3.10
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 02, 2015 at 04:34 PM
-- Server version: 5.5.42
-- PHP Version: 5.6.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `webservert`
--

-- --------------------------------------------------------

--
-- Table structure for table `csrf`
--

CREATE TABLE `csrf` (
  `cookieid` varchar(255) NOT NULL,
  `token` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `csrf`
--
ALTER TABLE `csrf`
  ADD PRIMARY KEY (`cookieid`,`token`);
