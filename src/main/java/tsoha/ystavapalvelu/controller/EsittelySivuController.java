package tsoha.ystavapalvelu.controller;

import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tsoha.ystavapalvelu.database.Database;
import tsoha.ystavapalvelu.models.page.EsittelySivu;
import tsoha.ystavapalvelu.models.page.EsittelySivuDao;
import tsoha.ystavapalvelu.models.user.Asiakas;

import java.sql.Timestamp;
import java.util.HashMap;

import static spark.Spark.*;

public class EsittelySivuController {


    private Database database;
    private EsittelySivuDao esittelySivuDao;

    public EsittelySivuController(Database database) {
        this.database = database;
        this.esittelySivuDao = new EsittelySivuDao(database);
        this.init();
    }

    private void init(){
        get("/mypages", (req, res) -> {
            HashMap map = new HashMap<>();
            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            if(sessioAsiakas == null) {
                res.redirect("/?norights=1", 302);
            }
            map.put("updated", "1".equals(req.queryParams("updated")));
            map.put("added", "1".equals(req.queryParams("added")));
            map.put("deleted", "1".equals(req.queryParams("deleted")));
            map.put("kirjautunut", false);
            map.put("kayttaja",  sessioAsiakas.getKayttajanimi());
            map.put("kayttajaid",  sessioAsiakas.getId());

            map.put("esittelysivut",esittelySivuDao.findAllWithOmistajaId(sessioAsiakas.getId()));

            return new ModelAndView(map, "omatesittelysivut");
        }, new ThymeleafTemplateEngine());

        get("/page/:pageId", (req, res) -> {
            HashMap map = new HashMap<>();
            Integer pageId = Integer.parseInt(req.params("pageId"));
            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            EsittelySivu sivu = esittelySivuDao.findOne(pageId);
            if(sessioAsiakas == null || (sessioAsiakas.getId() != sivu.getOmistaja_id() && !sivu.isJulkinen())) {
                res.redirect("/?norights=1", 302);
            }

            map.put("mypage", sessioAsiakas.getId() == sivu.getOmistaja_id());

            map.put("kirjautunut", false);
            map.put("kayttaja",  sessioAsiakas.getKayttajanimi());
            map.put("kayttajaid",  sessioAsiakas.getId());

            map.put("esittelysivu", sivu);

            return new ModelAndView(map, "esittelysivukatselma");
        }, new ThymeleafTemplateEngine());

        get("/page/:pageId/edit", (req, res) -> {
            HashMap map = new HashMap<>();
            Integer pageId = Integer.parseInt(req.params("pageId"));
            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            EsittelySivu sivu = esittelySivuDao.findOne(pageId);
            if(sessioAsiakas == null || (sessioAsiakas.getId() != sivu.getOmistaja_id() && !sivu.isJulkinen())) {
                res.redirect("/?norights=1", 302);
            }

            map.put("kirjautunut", false);
            map.put("kayttaja",  sessioAsiakas.getKayttajanimi());
            map.put("kayttajaid",  sessioAsiakas.getId());

            map.put("esittelysivu", sivu);

            return new ModelAndView(map, "esittelysivumuokkaus");
        }, new ThymeleafTemplateEngine());

        get("/pagelist", (req, res) -> {
            HashMap map = new HashMap<>();
            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            if(sessioAsiakas == null) {
                res.redirect("/?norights=1", 302);
            }

            map.put("kirjautunut", false);
            map.put("kayttaja",  sessioAsiakas.getKayttajanimi());
            map.put("kayttajaid",  sessioAsiakas.getId());

            map.put("esittelysivut", esittelySivuDao.findAll());

            return new ModelAndView(map, "esittelysivulistaus");
        }, new ThymeleafTemplateEngine());

        get("/sharedpages", (req, res) -> {
            HashMap map = new HashMap<>();
            return new ModelAndView(map, "esittelysivujaetut");
        }, new ThymeleafTemplateEngine());

        get("/newpage", (req, res) -> {
            HashMap map = new HashMap<>();

            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            if(sessioAsiakas == null) {
                res.redirect("/?norights=1", 302);
            }

            map.put("kirjautunut", false);
            map.put("kayttaja",  sessioAsiakas.getKayttajanimi());
            map.put("kayttajaid",  sessioAsiakas.getId());

            return new ModelAndView(map, "esittelysivuuusi");
        }, new ThymeleafTemplateEngine());

        post("/newpage", (req, res) -> {
            HashMap map = new HashMap<>();

            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            if(sessioAsiakas == null) {
                res.redirect("/?norights=1", 302);
            }
            String otsikko = req.queryParams("otsikko");
            String leipateksti = req.queryParams("leipateksti");
            boolean julkinen = Boolean.parseBoolean(req.queryParams("julkinen"));
            Timestamp now = new Timestamp(System.currentTimeMillis());
            EsittelySivu sivu = new EsittelySivu(-1, sessioAsiakas.getId(), otsikko, leipateksti, now, now, julkinen );

            esittelySivuDao.add(sivu);

            res.redirect("/mypages?added=1", 302);


            return "OK";
        });

        post("/page/:postId/edit", (req, res) -> {
            HashMap map = new HashMap<>();
            Integer postId = Integer.parseInt(req.params("postId"));
            EsittelySivu sivu = esittelySivuDao.findOne(postId);
            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            if(sessioAsiakas == null || sivu.getOmistaja_id() != sessioAsiakas.getId()) {
                res.redirect("/?norights=1", 302);
            }
            String otsikko = req.queryParams("otsikko");
            String leipateksti = req.queryParams("leipateksti");
            boolean julkinen = Boolean.parseBoolean(req.queryParams("julkinen"));
            Timestamp now = new Timestamp(System.currentTimeMillis());
            sivu.setOtsikko(otsikko);
            sivu.setLeipateksti(leipateksti);
            sivu.setJulkinen(julkinen);
            sivu.setMuokattu(now);

            esittelySivuDao.update(sivu);

            res.redirect("/mypages?edited=1", 302);


            return "OK";
        });

        get("/page/:postId/delete", (req, res) -> {
            HashMap map = new HashMap<>();
            Integer postId = Integer.parseInt(req.params("postId"));
            EsittelySivu sivu = esittelySivuDao.findOne(postId);
            Asiakas sessioAsiakas = req.session().attribute("asiakas");
            if(sessioAsiakas == null || sivu.getOmistaja_id() != sessioAsiakas.getId()) {
                res.redirect("/?norights=1", 302);
            }
            esittelySivuDao.delete(sivu.getSivu_id());

            res.redirect("/mypages?deleted=1", 302);


            return "OK";
        });

    }
}
