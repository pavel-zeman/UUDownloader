<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="cs" xml:lang="cs">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <title>UUDownloader - Hromadné stahování příloh z Unicorn Universe</title> 
  <style type="text/css">
    H1 { font-family: Arial,sans-serif; font-size: 15pt; }
    H2 { font-family: Arial,sans-serif; font-size: 12pt; }
    P, LI  { font-family: Arial,sans-serif; font-size: 11pt; }
  </style>
</head>
<body>

<h1>Základní popis</h1>
<p>Aplikace UUDownloader slouží pro hromadné stahování příloh vybraných artefaktů z Unicorn Universe.</p>
<p> 
Pro komunikaci s Unicorn Universe se používá přímo uživatelské rozhraní Unicorn Universe. 
Aplikace se tedy přihlásí do Unicorn Universe s použitím vašich přístupových kódů, prokliká všechny zadané artefakty a ze všech stáhne všechny přílohy.
Důsledkem použití tohoto postupu je to, že při změně uživatelského rozhraní Unicorn Universe přestane aplikace fungovat. 
Bohužel o lepším způsobu (API?) načítání dat z Unicorn Universe nevím.
</p> 

<h1>Instalace</h1>
<ol>
  <li>
    Pro běh aplikace je vyžadováno JRE verze 6 nebo vyšší na MS Windows (pro ostatní platformy viz <a href="#FAQ_LINUX">FAQ</a>).
    Pokud toto JRE nemáte k dispozici, nainstalujte si ho.
  </li>
  <li>Stáhněte si <a href="UUDownloader.zip?2012-03-11">aplikační archiv</a> a rozbalte ho do libovolného adresáře.</li>
  <li>Spusťte aplikaci <tt>UUDownloader.exe</tt> a nakonfigurujte následující údaje
  (všechny údaje si aplikace pamatuje v konfiguračním souboru <tt>config/UUDownloader.properties</tt>, některé údaje se šifrují):
    <ul>
      <li>Access code 1 a access code 2 pro přihlášení do Unicorn Universe.</li>
      <li>Cílový adresář, do kterého se mají přílohy z Unicorn Universe stahovat. Obsah tohoto adresáře je před stažením příloh <b>promazán</b>.</li>
      <li>Příznak, zda mají být přílohy každého artefaktu staženy do samostatného adresáře, nebo všechny přílohy do stejného adresáře.</li>
      <li>Seznam kódů artefaktů, jejichž přílohy chcete stáhnout (na každý řádek jeden kód).</li> 
  </li>
  <li>Spusťte stahování pomocí tlačítka <i>Stáhnout</i>. Otevře se nové okno informující o postupu stahování.</li>
</ol>

<h1>Historie verzí</h1>
<h2>2012-03-11</h2>
<p>První verze</p>


<h1>FAQ</h1>

<h2 >Jak je to s bezpečností celého řešení?</h2>
<p>Bezpečnost má v tomto případě několik aspektů:</p>
<ol>
  <li>
    Síťová komunikace - Všechna data jsou přenášena přes šifrované spojení (https), nemohou být tedy zachycena ani modifikována neoprávněnou osobou. 
  </li>
  <li>
    Konfigurační data - Konfigurační data (soubor <tt>config/UUDownloader.properties</tt>) obsahují citlivé údaje. Konkrétně jde o vaše přístupové kódy do Unicorn Universe.
    Všechna tato citlivá data jsou šifrována, nicméně šikovný Java programátor dokáže snadno zjistit šifrovací algoritmus a šifrovací klíč a data dešifrovat. 
    Doporučuji tedy k souboru <tt>config/UUDownloader.properties</tt> nastavit přístupová práva pouze pro vašeho uživatele.     
  </li>
</ol>

<h2><a name="FAQ_LINUX">Běží aplikace i pod Linuxem, Mac OS X nebo jiným mým oblíbeným operačním systémem?</a></h2>
<p>
Aplikace je implementovaná v Javě SE a měla by tedy fungovat pod libovolným operačním systémem, pro který je k dispozici JRE verze 6 nebo vyšší.
Pro spuštění aplikace pod jiným operačním systémem než MS Windows nelze samozřejmě použít dodávané EXE soubory, ale je nutné použít přímo JRE a aplikační JARy.
Aplikace se potom spouští následujícím způsobem (jde o jediný příkaz, který je potřeba napsat na jeden řádek, nebo rozdělit podle pravidel používaného prostředí):
</p>
<p>
<tt>java -cp lib/commons-logging-1.1.1.jar:lib/httpclient-4.1.2.jar:lib/httpcore-4.1.2.jar:lib/log4j-1.2.16.jar:config:lib/UUDownloader.jar -Xmx32m -Xms32m cz.pavel.uudownloader.UUDownloader</tt>
</p>
<p>Pro korektní fungování uvedeného příkazu je potřeba dodržet následující:</p>
<ul>
<li>Všechny uvedené cesty jsou relativní. Příkaz je tedy potřeba spouštět z adresáře, kde je nainstalovaná aplikace UUDownloader.</li>
<li>
  Aplikace je spuštěna se 32 MB Javovského heapu, což by mělo obecně stačit.
  Pokud se objeví nějaká chybová hláška o heapu, zkuste heap zvýšit (jde o parametry -Xmx a -Xms).
</li>
</ul>

</body>
</html>