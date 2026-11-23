package com.example.chariiptv.domain

data class GhibliMovieInfo(
    val title: String,
    val year: String,
    val director: String,
    val duration: String,
    val synopsis: String,
    val chronologicalIndex: Int = 99
)

object GhibliData {
    // 25 Películas oficiales de Studio Ghibli en orden cronológico histórico
    val NAUSICAA = GhibliMovieInfo(
        title = "Nausicaä del Valle del Viento",
        year = "1984",
        director = "Hayao Miyazaki",
        duration = "1h 57m",
        synopsis = "En un futuro postapocalíptico donde la Tierra está cubierta por una vasta selva tóxica repleta de insectos gigantes, la princesa Nausicaä lucha por comprender el ecosistema y evitar una guerra devastadora entre naciones humanas.",
        chronologicalIndex = 1
    )

    val EL_CASTILLO_EN_EL_CIELO = GhibliMovieInfo(
        title = "El castillo en el cielo",
        year = "1986",
        director = "Hayao Miyazaki",
        duration = "2h 4m",
        synopsis = "Pazu y la joven huérfana Sheeta emprenden una emocionante aventura aérea para localizar Laputa, una legendaria isla flotante con tecnología ancestral, mientras escapan de piratas celestes y ambiciosos militares.",
        chronologicalIndex = 2
    )

    val LA_TUMBA_DE_LAS_LUCIERNAGAS = GhibliMovieInfo(
        title = "La tumba de las luciérnagas",
        year = "1988",
        director = "Isao Takahata",
        duration = "1h 29m",
        synopsis = "En el Japón de finales de la Segunda Guerra Mundial, el joven Seita y su pequeña hermana Setsuko deben luchar por sobrevivir solos en un país devastado por los bombardeos, aferrándose al amor mutuo y la esperanza.",
        chronologicalIndex = 3
    )

    val MI_VECINO_TOTORO = GhibliMovieInfo(
        title = "Mi vecino Totoro",
        year = "1988",
        director = "Hayao Miyazaki",
        duration = "1h 26m",
        synopsis = "Dos niñas se trasladan al campo con su padre mientras su madre se recupera en el hospital. En el bosque cercano descubren un reino de criaturas mágicas y entablan una entrañable amistad con Totoro, el espíritu guardián.",
        chronologicalIndex = 4
    )

    val NICKY_LA_APRENDIZ_DE_BRUJA = GhibliMovieInfo(
        title = "Nicky, la aprendiz de bruja",
        year = "1989",
        director = "Hayao Miyazaki",
        duration = "1h 43m",
        synopsis = "Siguiendo la tradición de las brujas al cumplir 13 años, Nicky viaja con su gato negro Jiji a una ciudad costera para independizarse, donde funda un servicio de entregas a domicilio volando con su escoba.",
        chronologicalIndex = 5
    )

    val RECUERDOS_DEL_AYER = GhibliMovieInfo(
        title = "Recuerdos del ayer",
        year = "1991",
        director = "Isao Takahata",
        duration = "1h 58m",
        synopsis = "Taeko, una oficinista de 27 años de Tokio, viaja al campo para colaborar en la cosecha de flores de cártamo. Durante el viaje, los entrañables recuerdos de su niñez en 1966 resurgen, invitándola a replantear su vida y sueños.",
        chronologicalIndex = 6
    )

    val PORCO_ROSSO = GhibliMovieInfo(
        title = "Porco Rosso",
        year = "1992",
        director = "Hayao Miyazaki",
        duration = "1h 34m",
        synopsis = "En las aguas del mar Adriático en los años 20, un veterano piloto as de la Primera Guerra Mundial que fue transformado en cerdo trabaja como cazarrecompensas combatiendo piratas aéreos y defendiendo su libertad.",
        chronologicalIndex = 7
    )

    val PUEDO_ESCUCHAR_EL_MAR = GhibliMovieInfo(
        title = "Puedo escuchar el mar",
        year = "1993",
        director = "Tomomi Mochizuki",
        duration = "1h 12m",
        synopsis = "En una tranquila ciudad costera de Japón, la llegada de Rikako, una atractiva e independiente estudiante transferida de Tokio, pone a prueba la estrecha amistad entre los adolescentes Taku y Yutaka durante su último año de colegio.",
        chronologicalIndex = 8
    )

    val POMPOKO = GhibliMovieInfo(
        title = "Pompoko",
        year = "1994",
        director = "Isao Takahata",
        duration = "1h 59m",
        synopsis = "Frente a la rápida invasión urbana de Tokio en los años 60 que destruye sus colinas boscosas, una comunidad de mapaches tanukis con poderes de metamorfosis se une para luchar pacíficamente y defender su hogar.",
        chronologicalIndex = 9
    )

    val SUSURROS_DEL_CORAZON = GhibliMovieInfo(
        title = "Susurros del corazón",
        year = "1995",
        director = "Yoshifumi Kondō",
        duration = "1h 51m",
        synopsis = "Shizuku, una inquieta chica apasionada por la literatura, descubre con asombro que todas las fichas de los libros que saca de la biblioteca fueron previamente leídas por Seiji, un joven que sueña con ser luthier en Italia.",
        chronologicalIndex = 10
    )

    val LA_PRINCESA_MONONOKE = GhibliMovieInfo(
        title = "La princesa Mononoke",
        year = "1997",
        director = "Hayao Miyazaki",
        duration = "2h 14m",
        synopsis = "Afectado por una herida demoníaca mortal, el príncipe Ashitaka viaja a tierras lejanas y queda atrapado en un conflicto épico entre los dioses guardianes del bosque liderados por San y los habitantes de una ciudad minera industrial.",
        chronologicalIndex = 11
    )

    val MIS_VECINOS_LOS_YAMADA = GhibliMovieInfo(
        title = "Mis vecinos los Yamada",
        year = "1999",
        director = "Isao Takahata",
        duration = "1h 44m",
        synopsis = "Divertidas y tiernas viñetas cotidianas animadas en un estilo clásico de acuarela que ilustran las pequeñas alegrías, disputas, disparates y el incondicional amor de una familia contemporánea de clase media en Japón.",
        chronologicalIndex = 12
    )

    val EL_VIAJE_DE_CHIHIRO = GhibliMovieInfo(
        title = "El viaje de Chihiro",
        year = "2001",
        director = "Hayao Miyazaki",
        duration = "2h 5m",
        synopsis = "Chihiro, una niña de 10 años, queda atrapada en un mundo mágico gobernado por una bruja. Para rescatar a sus padres transformados en cerdos y volver al mundo humano, deberá trabajar en una mística casa de baños para los espíritus.",
        chronologicalIndex = 13
    )

    val HARU_EN_EL_REINO_DE_LOS_GATOS = GhibliMovieInfo(
        title = "Haru en el reino de los gatos",
        year = "2002",
        director = "Hiroyuki Morita",
        duration = "1h 15m",
        synopsis = "Tras salvar heroicamente a un gato de ser atropellado, la estudiante Haru descubre que rescató al Príncipe del Reino de los Gatos. Pronto es invitada a su reino fantástico, donde vivirá una mágica aventura con el Barón Humbert.",
        chronologicalIndex = 14
    )

    val EL_CASTILLO_AMBULANTE = GhibliMovieInfo(
        title = "El castillo ambulante",
        year = "2004",
        director = "Hayao Miyazaki",
        duration = "1h 59m",
        synopsis = "Sophie, una joven sombrerera, es víctima de una maldición de la Bruja Calamidad que la transforma en una anciana. Buscando revertir el hechizo, encuentra refugio como ama de llaves en el legendario castillo ambulante del mago Howl.",
        chronologicalIndex = 15
    )

    val CUENTOS_DE_TERRAMAR = GhibliMovieInfo(
        title = "Cuentos de Terramar",
        year = "2006",
        director = "Gorō Miyazaki",
        duration = "1h 55m",
        synopsis = "El archimago Gavilán recorre el mundo de Terramar para investigar el desbalance de la magia y la repentina aparición de dragones, encontrándose con Arren, un joven príncipe atormentado por su propia sombra.",
        chronologicalIndex = 16
    )

    val PONYO_EN_EL_ACANTILADO = GhibliMovieInfo(
        title = "Ponyo en el acantilado",
        year = "2008",
        director = "Hayao Miyazaki",
        duration = "1h 41m",
        synopsis = "Sosuke, un niño de 5 años que vive en lo alto de un acantilado, rescata a un pez dorado al que llama Ponyo. El fuerte deseo de Ponyo por convertirse en humana desencadena una fuerza ancestral que altera el equilibrio del océano.",
        chronologicalIndex = 17
    )

    val ARRIETTY_Y_EL_MUNDO_DE_LOS_DIMINUTOS = GhibliMovieInfo(
        title = "Arrietty y el mundo de los diminutos",
        year = "2010",
        director = "Hiromasa Yonebayashi",
        duration = "1h 34m",
        synopsis = "Arrietty, una valiente diminuta de 14 años, vive con su familia oculta bajo los cimientos de una vieja casa subsistiendo con pequeños objetos prestados, hasta que entabla una prohibida amistad con un humano enfermo.",
        chronologicalIndex = 18
    )

    val LA_COLINA_DE_LAS_AMAPOLAS = GhibliMovieInfo(
        title = "La colina de las amapolas",
        year = "2011",
        director = "Gorō Miyazaki",
        duration = "1h 31m",
        synopsis = "En el Yokohama de 1963, los estudiantes Umi y Shun unen a la comunidad estudiantil para defender su histórico club de la demolición ante los Juegos Olímpicos de Tokio, descubriendo lazos que unen sus orígenes familiares.",
        chronologicalIndex = 19
    )

    val EL_VIENTO_SE_LEVANTA = GhibliMovieInfo(
        title = "El viento se levanta",
        year = "2013",
        director = "Hayao Miyazaki",
        duration = "2h 6m",
        synopsis = "Una mirada poética y humana a la vida de Jiro Horikoshi, el visionario diseñador aeronáutico japonés que creó legendarios aviones, mientras vive un amor incondicional en una época de gran convulsión histórica.",
        chronologicalIndex = 20
    )

    val EL_CUENTO_DE_LA_PRINCESA_KAGUYA = GhibliMovieInfo(
        title = "El cuento de la princesa Kaguya",
        year = "2013",
        director = "Isao Takahata",
        duration = "2h 17m",
        synopsis = "Un modesto anciano cortador de bambú halla a una diminuta niña dentro de un tallo de bambú resplandeciente. Al crecer, se convierte en una deslumbrante doncella que cautiva a pretendientes reales antes de enfrentar su destino divino.",
        chronologicalIndex = 21
    )

    val EL_RECUERDO_DE_MARNIE = GhibliMovieInfo(
        title = "El recuerdo de Marnie",
        year = "2014",
        director = "Hiromasa Yonebayashi",
        duration = "1h 43m",
        synopsis = "Anna, una solitaria joven adoptada, es enviada a un pueblo costero por salud. Entre las marismas descubre una misteriosa mansión y conoce a Marnie, una chica rubia con la que entabla un lazo que desentrañará secretos del pasado.",
        chronologicalIndex = 22
    )

    val LA_TORTUGA_ROJA = GhibliMovieInfo(
        title = "La tortuga roja",
        year = "2016",
        director = "Michaël Dudok de Wit",
        duration = "1h 20m",
        synopsis = "Una fábula poética y sin palabras sobre un náufrago atrapado en una isla tropical desierta que, en su empeño por escapar en una balsa de bambú, es interceptado repetidamente por una enigmática tortuga roja marina.",
        chronologicalIndex = 23
    )

    val EARWIG_Y_LA_BRUJA = GhibliMovieInfo(
        title = "Earwig y la bruja",
        year = "2020",
        director = "Gorō Miyazaki",
        duration = "1h 22m",
        synopsis = "Earwig, una niña ingeniosa criada en un orfanato, es adoptada por una egoísta bruja llamada Bella Yaga. Decidida a no dejarse intimidar, usará toda su astucia y la ayuda de un gato parlante para dominar los secretos de la casa.",
        chronologicalIndex = 24
    )

    val EL_CHICO_Y_LA_GARZA = GhibliMovieInfo(
        title = "El chico y la garza",
        year = "2023",
        director = "Hayao Miyazaki",
        duration = "2h 4m",
        synopsis = "Tras la pérdida de su madre durante la guerra, Mahito se muda al campo. Guiado por una misteriosa garza real parlante, se adentra en un fascinante mundo mágico donde la vida y la muerte convergen en un nuevo comienzo.",
        chronologicalIndex = 25
    )

    private val MOVIES = mapOf(
        // 1. 1984
        "nausicaa del valle del viento" to NAUSICAA,
        "nausicaa" to NAUSICAA,
        "nausica" to NAUSICAA,
        "ndvdv" to NAUSICAA,

        // 2. 1986
        "el castillo en el cielo" to EL_CASTILLO_EN_EL_CIELO,
        "laputa" to EL_CASTILLO_EN_EL_CIELO,

        // 3. 1988
        "la tumba de las luciernagas" to LA_TUMBA_DE_LAS_LUCIERNAGAS,

        // 4. 1988
        "mi vecino totoro" to MI_VECINO_TOTORO,
        "totoro" to MI_VECINO_TOTORO,

        // 5. 1989
        "nicky la aprendiz de bruja" to NICKY_LA_APRENDIZ_DE_BRUJA,
        "kiki entregas a domicilio" to NICKY_LA_APRENDIZ_DE_BRUJA,
        "kiki" to NICKY_LA_APRENDIZ_DE_BRUJA,

        // 6. 1991
        "recuerdos del ayer" to RECUERDOS_DEL_AYER,

        // 7. 1992
        "porco rosso" to PORCO_ROSSO,

        // 8. 1993
        "puedo escuchar el mar" to PUEDO_ESCUCHAR_EL_MAR,
        "puedo oir el mar" to PUEDO_ESCUCHAR_EL_MAR,

        // 9. 1994
        "pompoko" to POMPOKO,

        // 10. 1995
        "susurros del corazon" to SUSURROS_DEL_CORAZON,

        // 11. 1997
        "la princesa mononoke" to LA_PRINCESA_MONONOKE,
        "mononoke hime" to LA_PRINCESA_MONONOKE,

        // 12. 1999
        "mis vecinos los yamada" to MIS_VECINOS_LOS_YAMADA,

        // 13. 2001
        "el viaje de chihiro" to EL_VIAJE_DE_CHIHIRO,
        "chihiro" to EL_VIAJE_DE_CHIHIRO,

        // 14. 2002
        "haru en el reino de los gatos" to HARU_EN_EL_REINO_DE_LOS_GATOS,
        "el regreso del gato" to HARU_EN_EL_REINO_DE_LOS_GATOS,

        // 15. 2004
        "el castillo ambulante" to EL_CASTILLO_AMBULANTE,
        "el increible castillo vagabundo" to EL_CASTILLO_AMBULANTE,
        "howl" to EL_CASTILLO_AMBULANTE,

        // 16. 2006
        "cuentos de terramar" to CUENTOS_DE_TERRAMAR,
        "terramar" to CUENTOS_DE_TERRAMAR,

        // 17. 2008
        "ponyo en el acantilado" to PONYO_EN_EL_ACANTILADO,
        "ponyo" to PONYO_EN_EL_ACANTILADO,

        // 18. 2010
        "arrietty y el mundo de los diminutos" to ARRIETTY_Y_EL_MUNDO_DE_LOS_DIMINUTOS,
        "el mundo secreto de arrietty" to ARRIETTY_Y_EL_MUNDO_DE_LOS_DIMINUTOS,
        "arrietty" to ARRIETTY_Y_EL_MUNDO_DE_LOS_DIMINUTOS,

        // 19. 2011
        "la colina de las amapolas" to LA_COLINA_DE_LAS_AMAPOLAS,

        // 20. 2013
        "el viento se levanta" to EL_VIENTO_SE_LEVANTA,
        "se levanta el viento" to EL_VIENTO_SE_LEVANTA,

        // 21. 2013
        "el cuento de la princesa kaguya" to EL_CUENTO_DE_LA_PRINCESA_KAGUYA,
        "princesa kaguya" to EL_CUENTO_DE_LA_PRINCESA_KAGUYA,

        // 22. 2014
        "el recuerdo de marnie" to EL_RECUERDO_DE_MARNIE,
        "marnie" to EL_RECUERDO_DE_MARNIE,

        // 23. 2016
        "la tortuga roja" to LA_TORTUGA_ROJA,

        // 24. 2020
        "earwig y la bruja" to EARWIG_Y_LA_BRUJA,
        "earwig" to EARWIG_Y_LA_BRUJA,

        // 25. 2023
        "el chico y la garza" to EL_CHICO_Y_LA_GARZA,
        "el nino y la garza" to EL_CHICO_Y_LA_GARZA,
        "kimitachi" to EL_CHICO_Y_LA_GARZA
    )

    fun getMovieInfo(channelName: String): GhibliMovieInfo {
        val normalized = normalize(channelName)
        MOVIES[normalized]?.let { return it }
        for ((key, info) in MOVIES) {
            if (normalized.contains(key) || key.contains(normalized)) {
                return info
            }
        }
        return GhibliMovieInfo(
            title = channelName,
            year = "Studio Ghibli",
            director = "Hayao Miyazaki / Isao Takahata",
            duration = "Película",
            synopsis = "Obra cinematográfica maestra producida por Studio Ghibli. Audio en español latino y calidad optimizada para pantalla completa."
        )
    }

    fun getChronologicalIndex(channelName: String): Int {
        val info = getMovieInfo(channelName)
        return info.chronologicalIndex
    }

    private fun normalize(name: String): String {
        return name.lowercase()
            .replace("á", "a")
            .replace("à", "a")
            .replace("ä", "a")
            .replace("â", "a")
            .replace("é", "e")
            .replace("è", "e")
            .replace("ë", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ì", "i")
            .replace("ï", "i")
            .replace("î", "i")
            .replace("ó", "o")
            .replace("ò", "o")
            .replace("ö", "o")
            .replace("ô", "o")
            .replace("ú", "u")
            .replace("ù", "u")
            .replace("ü", "u")
            .replace("û", "u")
            .replace("ñ", "n")
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
    }
}
