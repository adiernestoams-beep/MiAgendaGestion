package pe.agenda.gestion;

import java.util.List;

/** Enlaces oficiales de carpetas Drive creadas para los eventos de 365 Agenda. */
public final class DriveLinks {
    private DriveLinks() {}

    private static final String BASE = "https://drive.google.com/drive/folders/";

    public static void apply(AgendaDb3 db) {
        // Septiembre
        link(db,"2026-09-23","almuerzo", "1ven00wDPb_zSnvRQFEHpO7zt0b3X-BcZ","13Yh78uJJCYcEueTC15WeKVTIyKMotnxY","1K5gnbc7EkyAbLb4A-xJeuwCWrEVahJPq");
        link(db,"2026-09-30","reunion mensual de coordinacion academica", "1jER_MpbUqyArrhWbuSVMKMBLF5BHXxR6","1KeXIGIu7o_iFx9x1h9w52hP2AP3uKjjW","1_ZzvWr1dJnzMJPCxPhni1jhEqQIAJxEV");
        link(db,"2026-09-30","educacion adventista", "1_yb9CQ54QaEmQ33ZtMMslFVhyCwoKKpn","1z_50a1xI6gBJjyqT7iC12xVzOH65a5wC","1NCquHxmwvPIy7VxFmzrnTCGd_UkMbZZ7");

        // Octubre - calendario oficial
        link(db,"2026-10-01","comprobando mis conocimientos", "1EAcNsOnQ-cLNVhUWPX3j0HiweT-WErJo","1k6cq1RazlpC1va0Q2b1S-5BAKBb86NhE","12uPTnQ50DZqYRXynmqaiwE8r8ayWmRnk");
        link(db,"2026-10-02","ruth arapa", "1OiFb5tMUTH1emUhA68r4cxm4Ab0IKBRy","1H1lnz5vJzu4k8UEHpbLj2462z9MubiRk","11lXnCqK0MJ4lF4Bn4PBzrSkok3-ME1E6");
        link(db,"2026-10-02","dia de la sonrisa", "1NGAd3SNUnyhALVgGL_UM7OvuX6_hekJK","1bVjq87-urhBvwOSmCjVRRT6sx8vH3LhC","1qMWDeeA_b9f-BTq2jq9kEiieW7J_wBOs");
        link(db,"2026-10-05","salud mental", "1DpAUPAW5nvNIh-dHHSoDwOtFirvCKoQX","1z0RbO73F1KcbvfTchkHtFzsajNgEpLC7","11IviLPdJuJRPH2zNDKEH4o9xmI4SuCnG");
        link(db,"2026-10-05","receso para estudiantes", "1BWMtc2Q9vipdGmeg5qDtHBdKSDHxzjYl","18zluvv1JoY-OocS0CF74_FC_Z5Ysdi3h","1sru76liRJM4ejWTTAA6yIdplmPzY_6B0");
        link(db,"2026-10-06","salud mental", "1AK9qXBrzay2uIt0-YshcnMb5rdY5w75D","1N1knMGw9GeAioVy_CTg5c4Xape2yF4bc","1gJOyFjodSYvMlRRMQwZHywFCUR-AMd7Z");
        link(db,"2026-10-06","luz de los andes", "14JQrrfYnubF3LLDfGAXQ3OicoUcTM5NO","1eq4c3-sZh5qsv-gTILCmlUDQmZu3ho-M","159GoMXSPHLZ4CyzZoZcYeY9MPO2dloib");
        link(db,"2026-10-07","unidades de aprendizaje", "19KbxwocvxyHfnnQfLZw3q7dCdWtuSvic","1z94mx80GG6V7KymewpPHA0zj7NVWVWzh","1OLmAC8GybNu12HUAflPFNS1uN75ixxZN");
        link(db,"2026-10-07","almuerzo de confraternidad", "1Sk6WpzjscNV0Luat1SiGp7UDRdUd4J9U","1OTDatn9Vq2BtL0dvmcQ4m4Xt0eWpq7CY","1v0lB0AGBbWCFSRt4z2mI61Y_GJt2Fvg7");
        link(db,"2026-10-08","feriado", "1y-B1eHYG6qtboYEb05tWn0g187gcRPA7","1lgKnfBDLybuDSNpkUPOmf8RhOJvPdsxv","1NcjYoes4wcqnABJlsTaVQJWBVln78geX");
        link(db,"2026-10-09","sesiones de aprendizaje", "1foGYHAE4l86nW32cXZlYx3fKYDEm137K","1mJOvdm_8B5M5OgGop5N529360ke-42BE","1SaBd4gMPp5ygBRaOn5X356KQD-kVWD1e");
        link(db,"2026-10-12","milagros segarra", "1eMS6INy_xl-g7XjPYQjZlEE_NucnoZkb","1rBuR_9QYLYEOIq1VqrgCWN6mPsJc3ixl","1wJMlckMYxR7gqD5yZ5ivLGmlHfxJouD4");
        link(db,"2026-10-12","inicio del iv bimestre", "1kYsZMCl6PdIdmpLhDKQFFmKAboUxWaJj","1OwQZZeUJWKFtT_F8--X_Ipf2S_qdfyOy","1CVeZhsZKTOnKe45eKVCY1t9q3NPxqPzf");
        link(db,"2026-10-12","impacto esperanza", "1HpgEQNYvu9DWV-QUZy7qDXcOoLJhj7AH","1Oy0tgRDf4peWTYGrpdVaEDeR1qqYeofG","1ZFbBOC4Iam2o7h08anhllKm7wo3NH-BV");
        link(db,"2026-10-13","leslie quispe", "1a9WOXwTCC8brWG3cYZ0h1Gp-j5jlWzas","1IGtmlmM44QBOkWn--OLp3Ycy7b68jib3","12TWvF55IfpeTkt0mTSYKzvDav-TVTPPs");
        link(db,"2026-10-13","salida de estudiantes", "1ztJAQm9aze3iIEGw0_Z0rE0lryJGsgnM","1orOnfpDvsw-7nhJtsORI_3LQ-bZvKYS7","1ozR1e1xpxPBAzw7j_PFxfLonRh6fa3Bn");
        link(db,"2026-10-14","taller de padres", "1qjWX_7V-_gLmTGqcQ7n6ZTIHoOOwbJd5","1o9WV44_Ply13wDsNUt6FtfLcJwWMcawt","14Ik10lXQ7pnX9Zj_iCRK-IW9eTzF4az9");
        link(db,"2026-10-14","quimicat", "19TSfzrW7YKwIVDJJycL0iqSW0XsyI45J","1djvhvi0_lQH6lFdVGt0LlM5bBd_3S7pF","1TgDwQeNXi4urXkqb2kXzIGJMyRUXDF8L");
        link(db,"2026-10-15","concurso de bandas", "1xO6iC9PatCQp1oA2m7c_ipWpCJCxTrd-","1kIomEaEqSoYLi9-CS2ee_bbcjFAUGRrz","129stjL1D_keC56E4hIcP0vEzf4sSuZrw");
        link(db,"2026-10-18","ludwing chambi", "1cdJ-nP6xtvKHyVHeggYVgaeQ27CGOZR_","15Y_x2dMnWmjMbwye97jTomd52Y9xCFpb","1pGnlJIfHYc70LH0dcYOZydFlad2HRXAn");
        link(db,"2026-10-19","evelin lopez", "1NHsPJ6E8aMA8CwhLoJxm7W8O7oBNceWP","1ti3s-w2w2aadIKiBAUStbaFfFwgupCh-","1QZf8orDZ6pxgB_A8f0v5fPNsmtK8aGAi");
        link(db,"2026-10-19","olimpiadas de matematica", "1kSDv2NJQu9PGP8PTMavFgd9O2lfwyO__","15tDBTa-EIbYHOsWg77Zwv4zut1YpltoF","1rep7aP2zM5TgtbzsaB6Xa_RdoNW7SOfg");
        link(db,"2026-10-19","semana de la creacion", "120iYCWyYBK-6RpWB2OdFXGXW9Vhf18d4","1imlSNfeyAvw607p4E1jE0pcjsBJ6DHuP","1t4W6BCzhySOq7eO1VJMGnLLU6QCVQ0ob");
        link(db,"2026-10-21","yhonny alejo", "1XGVqmp032W6MtLdujQeUAckSXn3EbH_g","1EQn0p-9kER2Hi2-m86Bu55Oly_IWR3Lw","18iltRI9ZEGLAa1rhYANKQgEyDVrkxap1");
        link(db,"2026-10-21","salida de viaje", "1gXf_eUX816m0biEwApWm7yTmrSgDO4rX","1zInnNYztLAPvdM6NJ5-hWHUZnpm5KFtw","1QXNfzSIlvs5PHU2B9MDnrrXQdW2uL5jg");
        link(db,"2026-10-22","viaje de estudios", "1gXf_eUX816m0biEwApWm7yTmrSgDO4rX","1zInnNYztLAPvdM6NJ5-hWHUZnpm5KFtw","1QXNfzSIlvs5PHU2B9MDnrrXQdW2uL5jg");
        link(db,"2026-10-22","voices in praise", "1ZGiozxR8Txn9LtoTMa6qMedzYmLaUPeE","1QWEVMJlqIIaSteghH7Jf1FNO5E7FQLSP","1VYnw5rfX702AcMJ0w8o-I66TJeV6rkVJ");
        link(db,"2026-10-22","spelling bee", "19gWB_FA2o7VYKoKzCjAiWJhQTK1oxteW","1wHshPFFHl2w8J8T4bgt7KLdphSJLj6k6","1IhM3PSLJHSJgcSacdi3RFDcFCH0JwiBS");
        link(db,"2026-10-25","luis torres", "1bEphQavHvT6tGmp211wngdkjXfmcpyea","15rFmU5Fp5jWJKHXqapgasprg30TGr_po","191ltCdMek2TuRkKbKjiNy_wKKv8MC8-s");
        link(db,"2026-10-26","paee", "1_enUvDi1KjRiNAlxtyzK2S9ZakN0XVS6","1li0rf3b3lDzbAM9itS6OttpvgwIvpOhU","1agxIvXrf9wAa9fFapz3OBVoAFWO_lD2s");
        link(db,"2026-10-28","lanzamiento de matricula", "1rIOYvEOJx623qxNjxu58bcUudJlNtJv4","1GeJFR2BG4kejuHwbOpDMajTnL7m7FQH1","1Jr-A6DaJ-AEPM_AgKnOMTarnH5I3BYyK");
        link(db,"2026-10-29","reunion de coordinacion academica", "144OntNge13s0H4gJT2kBSOkqNzJJHZKM","1OHkp4MybERFlY3vcZod3LmzrsmCOzYlO","1VaWLXpLQk-LYaBuZ8utRXgfpMH40mppX");
        link(db,"2026-10-31","quinto sabado misionero", "10n6cCxeiqYTN-VCTGB-fi5zlbt8o_pR6","1RQnvEMQ-8LyVySSjg0wdJwjT8mufR7YY","1lKQX6c0PDRwfT8jU-7uDeONA-SEzc2S_");

        // Noviembre
        link(db,"2026-11-02","torneo de robotica", "1fp96fdgZNVV69Q-BZWCbfx3V-iXQfq11","16rmBoAlxegUkAI9-b1CNoMyW9C8GAgLE","1n_gvn3_gQv3x87mu2G1cquOjWVI0pw-R");
        link(db,"2026-11-03","reunion mensual de coordinacion academica", "1UgTCRjRcDI5CQct8Tks_WxoWD5o4NKlc","1WdDdVanyy5T9PF3li8lkP1u-dqqlDW-8","1rVEne8KMct0O6dJlGoiluJh-7hnA1zYT");
        link(db,"2026-11-05","convencion de estudiantes", "1tnL32A6MHniyRnA-o-YikG23Eoa3dQ7a","1sq2efxkUb7Ne-k-cKu5360_1qPFm6WCC","1eMTvKRDRiGJr2JuNHgdDur1k8b9nFmzl");
        link(db,"2026-11-10","informe de avance", "16nKyYYEMyQ0MPcuvsD8vXUN49YEERpll","19QdWmqwhDJli2y_9fLKyXAasUu_ovW7R","1YUfD72ndwTDCl9FG6f5FTCy8Y6MJAJsl");
        link(db,"2026-11-11","escuela de padres", "1ww-8lNH9grgTMuJj9B5ahZ4ZSm6EChHR","1ZGu19wI14DZpZTQVH86O5O8JKatcQZqx","1L-9kKszIDDchCZjETCJNOAw1FK73j7XR");
        link(db,"2026-11-12","alameda de los lectores", "1wPyHMq0uXAtzF0rN_Fm564QP8MwoZtZc","1OU-1oifcU6GzS_mgJV__NkO3pxSzqkrL","1HlioVGgd9-IFejQZVP6IYni-sh9Sy4_u");
        link(db,"2026-11-12","educacion primaria", "1SLpHLYxSWxAS6WeLKlyL-8IfaPgmJ1Jr","1_zI1C-h419Gu5Yz1_W1Vf4Miwr9wXxR3","1Q-ceKN0RYPxbBJ4Y_o4fh3q6_xONMtdO");
        link(db,"2026-11-12","mural letrado", "16GKnzq8bIZswbs2hbFdB9hh59QdaiAmW","1L0AIEUkjGrI7T1i2KfuQFi3FBpeY4-mP","1tY8ty7ISlkZiTKxZ_jIOsG14LAUyu06q");
        link(db,"2026-11-14","gimnasia ritmica", "1O4jsCheq5gcLtczbP8cjpB0GeDHsz1ki","1WetAWoHJ11nMUYOo-pLy4QCC9_NcAP9S","1bZTqJlNZkJIS_WOeciiwtu0AUm5fkF2J");
        link(db,"2026-11-19","reunion mensual de coordinacion academica", "1od4qligbTYtBp1YxhmaVlvD8B0LLu31k","1I6KzJC8csn-6-axuKD6sGT3aOltk0V50","1AtHrh62tPOtPmnZw4eqPZdT9KzWihnRL");
        link(db,"2026-11-20","torneo nacional de robotica", "1YiXPXRyY6U2A-E5O8brJ7TpbUbt_mwQx","1cjRX6Q_byCPtDzXGfaeTwOT_CuGfXhLh","16s3PGF5RRSSijEoxMcjtLAwCpxvLguq0");
        link(db,"2026-11-21","investidura ministerio vital", "1z1Dm3K_OvE1Ic0uSO_ebOolKX1mIMpWp","1gy9Z-YzchtYZKgGUpJa2468iD-dLuW-L","1c0cx_-wcJSqzyIIlbt2zpxykRG-YcqdB");
        link(db,"2026-11-23","elecciones del municipio escolar", "1GIVWhiyD7SLh7kf716TtKnPez0tarhJa","1sRC_nBNDvyGVQRKiYP6-J7ChbfwUWCos","1Ra91CoxLwLx0tvT_WhituXjZ18I2eZn2");
        link(db,"2026-11-25","no violencia contra la mujer", "19yeWoMATsOcb_s-YrB57uZl5mJjREuNb","1c61ND8h1ZZPpACXiatf0h9sOC1vjXuCL","1L2stxeRa3-ikcxvx_XaQsj-EyQ2jQs5L");
        link(db,"2026-11-25","luz de los andes", "1NQVY1NXGveATqwRYBmd98_-LKwCnXio7","1AukE92dBQKOxnrx59MrGrvjkZ7VdVRg7","1gfAZ4MAlmiP6m5JF-vhGE0ahqY5kd7fQ");

        // Diciembre
        link(db,"2026-12-02","comprobando mis conocimientos", "1SZPpW9__gPV07hsX-zVv4RukigQ19Nwg","1uL9WQOLCmehTj9u-9YkUg5i5I8XJXra7","1Y8bhK4_4JP3X2wj1mDs9Lrw07te3KvCW");
        link(db,"2026-12-04","cubo rubik", "1tFxANhYyaNqtrMLXKC70MkPcs5-frK35","1G4aV4mwXb3cDDP0OGdrxU5jzlPr5yzlw","1bUGT99Cjwa6qg-vqv41XH6TvR2OgtmyL");
        link(db,"2026-12-11","cambio de mando", "1C6RyjpUEJ7fQDfK5vOhin8GCKUNd1WsH","1AjaMsF83y1F28aVpGhUy27X5KdbAb8P5","1gSdQ0H5T-1WnTkxtZQKQn36HPJp-NjV4");
        link(db,"2026-12-13","cierre de actividades espirituales", "1ZDs32UzNeEwSh-onD8940WjKJOQxWs34","16BNTRglhi88xZfuuJObeB7hBPjUJ77WC","1CbauPTcn79jfoSmSkvmG018Khc02OQ6Q");
        link(db,"2026-12-14","mejorando nuestra ortografia", "14PSO1KOHYOQmSMWgMBhQkchiOKHRspwr","1xPKQiU-7-ULrkywtjTi2eoboWr7JiREN","1d-KTVqXVpt9qk6pgTTIrBi-Rthu6A-V1");
        link(db,"2026-12-15","cantata navidena", "1MpYsy0IZJFL06UWoUyy0edtZmSKvIcg0","12938btZKwZkLpRty3J4LR0i1lDHW13_C","1a4Lv-fGQLVno9rwqzcdlM01vlKe2fw6A");
        link(db,"2026-12-17","compartir de fin de ano", "1djaAEJm5qxFKLMIyqZr-6fWfHiSjR6eH","1pjEOrCmBmyly0COpPuQDfGXbV_fif-Uo","1o1VZl3EYnbc-sw-Mq92q89fWA-6LFggu");
        link(db,"2026-12-19","mas amor en navidad", "17npLBE0-ZHcRTifbuJaAGGOhTkH-RG7t","1xaXjt2dvuP7B9yjyBPwgGDTLc4b9e_XB","14uPUoFsVud_VjpXHR-xOr0M6rml6tZeB");
        link(db,"2026-12-21","graduacion y clausura", "1ndjjjVtPATpKb3v06TdSOr4IqJMC3Ks9","1NO3Nfp2jFKhRJIP9cFQDStA0c3GpdNH_","1w2GrejEOLxmzRQx5jylHHYF8Q3cKyKfo");
        link(db,"2026-12-23","almuerzo de confraternidad", "1gS5KjK4QfUonASzguq_O_fZYAyDZ_9XW","1HaR7I6rUyQd1V_JB_2AU2Fotz2t-00GJ","1EjCDLE2rIFBoNCuoOTFQRlMZQTRxuxKe");
    }

    private static void link(AgendaDb3 db, String date, String titleFragment, String folderId, String videoId, String fotosId) {
        List<AgendaDb3.Event> events = db.search(2026, date);
        String needle = AgendaDb3.normalize(titleFragment);
        for (AgendaDb3.Event e : events) {
            if (!AgendaDb3.normalize(e.title).contains(needle)) continue;
            e.driveFolderUrl = BASE + folderId;
            e.driveVideoUrl = BASE + videoId;
            e.driveFotosUrl = BASE + fotosId;
            db.update(e);
        }
    }
}
