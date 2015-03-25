-- phpMyAdmin SQL Dump
-- version 4.3.10
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Gegenereerd op: 25 mrt 2015 om 11:35
-- Serverversie: 5.5.42
-- PHP-versie: 5.6.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `webservert`
--

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ondersteuners','beheerders') NOT NULL,
  `cookieId` varchar(32) DEFAULT NULL
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Gegevens worden geëxporteerd voor tabel `user`
--

INSERT INTO `user` (`id`, `username`, `password`, `role`, `cookieId`) VALUES
(1, 'thierry', 'c5fd3e56650b8725c7e1f8f7f16cad46fbf48bc5143470e635e7f744a098edaf', 'beheerders', NULL),
(2, 'johan', '12b239b2304f5eec18b83e64cf2405e371363452fa8953bc1498cf342421a62c', 'ondersteuners', 'd2gktio9quqg011g3o5403rekh');

--
-- Indexen voor geëxporteerde tabellen
--

--
-- Indexen voor tabel `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT voor geëxporteerde tabellen
--

--
-- AUTO_INCREMENT voor een tabel `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;