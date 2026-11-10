package com.example.data

enum class EducationalFormat(val labelRu: String) {
    ARTICLE("Научная статья"),
    VIDEO("Видео-лекция"),
    INFOGRAPHIC("Инфографика")
}

enum class EducationalTopic(val titleRu: String) {
    TRIGGERS("Триггеры"),
    MECHANISMS("Механизмы зависимости"),
    CRAVING_STRATEGIES("Преодоление тяги"),
    SOCIAL_SUPPORT("Социальная поддержка"),
    HEALTHY_HABITS("Здоровые привычки"),
    RELAPSE_RECOVERY("Восстановление после срыва")
}

data class InfographicMetric(
    val label: String,
    val valueText: String,
    val fraction: Float, // 0f..1f
    val commentary: String
)

data class VideoChapter(
    val timeCode: String,
    val chapterTitle: String,
    val slideSummary: String
)

data class EducationalMaterial(
    val id: String,
    val format: EducationalFormat,
    val topic: EducationalTopic,
    val title: String,
    val durationOrReadTime: String,
    val summary: String,
    val bodyParagraphs: List<String>,
    val keyScientificFacts: List<String>,
    val infographicMetrics: List<InfographicMetric> = emptyList(),
    val videoChapters: List<VideoChapter> = emptyList()
)

data class AchievementMilestone(
    val id: String,
    val title: String,
    val description: String,
    val rewardTitle: String,
    val currentValue: Int,
    val targetValue: Int,
    val categoryLabel: String
) {
    val isUnlocked: Boolean
        get() = currentValue >= targetValue

    val progressFraction: Float
        get() = if (targetValue <= 0) 1f else (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
}

object EducationAndAchievementsCatalog {

    val educationalMaterials: List<EducationalMaterial> = listOf(
        // 1. ТРИГГЕРЫ (Статья + Инфографика)
        EducationalMaterial(
            id = "edu_triggers_infographic",
            format = EducationalFormat.INFOGRAPHIC,
            topic = EducationalTopic.TRIGGERS,
            title = "Анатомия триггеров: какие ситуации чаще всего провоцируют тягу",
            durationOrReadTime = "Инфографика • 4 мин",
            summary = "Наглядный разбор внутренних и внешних пусковых механизмов, запускающих условный рефлекс выпивки.",
            bodyParagraphs = listOf(
                "Триггер — это любой сигнал (место, время суток, эмоция, запах или усталость), который мозг за годы употребления связал с приемом алкоголя. Сам по себе триггер не заставляет человека пить — он лишь включает кратковременную условную реакцию ожидания дофамина.",
                "Внутренние триггеры (голод, обида, тревога, одиночество, вечернее переутомление) опаснее внешних, потому что мы носим их внутри себя. Распознание триггера по имени лишает его половины власти."
            ),
            keyScientificFacts = listOf(
                "До 68% срывов происходят в промежутке с 17:30 до 21:00 на фоне падения глюкозы (голода) и усталости.",
                "Условный рефлекс на старый маршрут мимо алкомаркета угасает в 3 раза быстрее, если сменить дорогу домой на первые 30 дней."
            ),
            infographicMetrics = listOf(
                InfographicMetric(
                    label = "Вечерняя усталость и голод (HALT)",
                    valueText = "68% случаев тяги",
                    fraction = 0.68f,
                    commentary = "Сытный ужин до 18:30 снижает вечернюю тягу более чем вдвое"
                ),
                InfographicMetric(
                    label = "Конфликты, обида и стресс",
                    valueText = "54% случаев",
                    fraction = 0.54f,
                    commentary = "Дыхание 4-7-8 тормозит выброс кортизола за 2 минуты"
                ),
                InfographicMetric(
                    label = "Одиночество и скука по выходным",
                    valueText = "47% случаев",
                    fraction = 0.47f,
                    commentary = "Заранее составленный план на субботу и воскресенье защищает от пустоты"
                ),
                InfographicMetric(
                    label = "Привычные компании и застолья",
                    valueText = "39% случаев",
                    fraction = 0.39f,
                    commentary = "В первые 90 дней рекомендуется избегать пьющих компаний"
                )
            )
        ),

        // 2. МЕХАНИЗМЫ ЗАВИСИМОСТИ (Видео-лекция)
        EducationalMaterial(
            id = "edu_mechanisms_video",
            format = EducationalFormat.VIDEO,
            topic = EducationalTopic.MECHANISMS,
            title = "Видео-лекция: Как алкоголь перехватывает управление дофамином и ГАМК",
            durationOrReadTime = "Видео-урок • 6 мин",
            summary = "Почему после первой рюмки пропадает тормоз и как восстанавливаются рецепторы головного мозга.",
            bodyParagraphs = listOf(
                "Этанол одновременно бьет по двум главным системам мозга: искусственно подстегивает тормозную систему ГАМК (давая временное расслабление) и блокирует возбуждающую систему глутамата.",
                "В ответ мозг уменьшает чувствительность ГАМК-рецепторов и выращивает лишние рецепторы глутамата. Когда спирт уходит из крови, остается «голый» избыток глутамата: возникает тремор, тревога, бессонница и скачок пульса."
            ),
            keyScientificFacts = listOf(
                "Феномен потери контроля над дозой связан с отключением префронтальной коры уже через 15 минут после первой порции этанола.",
                "Плотность дофаминовых рецепторов D2 восстанавливается до возрастной нормы при полной трезвости."
            ),
            videoChapters = listOf(
                VideoChapter(
                    timeCode = "00:00 — 01:30",
                    chapterTitle = "Глава 1. Обман дофаминовой системы",
                    slideSummary = "Алкоголь вызывает нефизиологический выброс дофамина в прилежащем ядре мозга, заставляя мозг запоминать спирт как «сверхценность» выше еды и отдыха."
                ),
                VideoChapter(
                    timeCode = "01:30 — 03:45",
                    chapterTitle = "Глава 2. Качели ГАМК и Глутамата",
                    slideSummary = "Почему на утро после выпивки наступает паника и сердцебиение: мозг остается с избытком возбуждающего глутамата без естественного тормоза."
                ),
                VideoChapter(
                    timeCode = "03:45 — 06:00",
                    chapterTitle = "Глава 3. Нейропластичность и полное исцеление",
                    slideSummary = "Как за 30–90 дней трезвости мозг заново выстраивает здоровые рецепторы и возвращает способность радоваться простым вещам."
                )
            )
        ),

        // 3. СТРАТЕГИИ ПРЕОДОЛЕНИЯ ТЯГИ (Статья)
        EducationalMaterial(
            id = "edu_craving_strategies_article",
            format = EducationalFormat.ARTICLE,
            topic = EducationalTopic.CRAVING_STRATEGIES,
            title = "Научные стратегии преодоления острой тяги: правило 15 минут",
            durationOrReadTime = "Статья • 5 мин",
            summary = "Доказанные приемы когнитивно-поведенческой терапии и физиологии, позволяющие пройти пик желания выпить без срыва.",
            bodyParagraphs = listOf(
                "Первое и главное открытие современной аддиктологии: острая тяга к алкоголю не длится часами. Подобно морской волне, она нарастает за 3–5 минут, держится на пике около 7–10 минут и неизбежно идет на спад, если вы не подкармливаете её внутренним торгом.",
                "Стратегия №1 — «Отсрочка решения на 20 минут». Скажите себе: «Я не борюсь с мыслью навсегда, я просто откладываю любое действие ровно на 20 минут, а пока плотно поем и выпью два стакана воды».",
                "Стратегия №2 — «Экстренная смена физиологии». Наполнение желудка теплой пищей и водой активирует блуждающий нерв и выключает сигнал голода в гипоталамусе, который у зависимых людей напрямую спаян с центром тяги.",
                "Стратегия №3 — «Назови врага вслух». Проговорите: «Сейчас во мне говорит постабстинентный симптом, через четверть часа он пройдет». Это мгновенно возвращает активность лобным долям мозга."
            ),
            keyScientificFacts = listOf(
                "Прием 400 мл воды и сложных углеводов снижает интенсивность тяги на 40–55% в течение 12 минут.",
                "Умывание лица холодной водой включает «рефлекс ныряльщика» и замедляет пульс на 10–15 ударов в минуту."
            )
        ),

        // 4. ВАЖНОСТЬ СОЦИАЛЬНОЙ ПОДДЕРЖКИ (Статья + Инфографика)
        EducationalMaterial(
            id = "edu_social_support_article",
            format = EducationalFormat.ARTICLE,
            topic = EducationalTopic.SOCIAL_SUPPORT,
            title = "Почему социальная поддержка и общение увеличивают успех в 3 раза",
            durationOrReadTime = "Статья • 4 мин",
            summary = "Нейробиология окситоцина, преодоление стыда и роль круга трезвых единомышленников.",
            bodyParagraphs = listOf(
                "Зависимость процветает в тайне, стыде и одиночестве. Когда человек пытается бороться один на один со своими мыслями в четырех стенах, уровень гормона стресса кортизола остается хронически высоким.",
                "Живое участие, честный разговор с близкими или короткое сообщение поддержки ровеснику в трезвом сообществе запускают выработку окситоцина и эндогенного серотонина. Мозг получает естественное чувство безопасности без всякой химии.",
                "Даже когда вы сами поддерживаете новичка, который держится всего третий день, в вашем мозге укрепляется идентичность сильного, выздоравливающего человека («эффект помогающего терапевта» Рисмана)."
            ),
            keyScientificFacts = listOf(
                "По данным многолетних наблюдений, люди, участвующие в сообществах взаимной поддержки, сохраняют годовую ремиссию в 2,8 раза чаще.",
                "Открытое обсуждение своего состояния с лечащим врачом и близкими снимает до 70% внутреннего напряжения."
            ),
            infographicMetrics = listOf(
                InfographicMetric(
                    label = "Устойчивая трезвость при поддержке сообщества и семьи",
                    valueText = "74% успеха",
                    fraction = 0.74f,
                    commentary = "Окситоцин и живое участие защищают от вечерней тоски"
                ),
                InfographicMetric(
                    label = "Попытки бросить в полной изоляции и скрытности",
                    valueText = "26% успеха",
                    fraction = 0.26f,
                    commentary = "Скрытность усиливает стыд и внутреннее напряжение"
                )
            )
        ),

        // 5. ЗДОРОВЫЕ ПРИВЫЧКИ (Инфографика)
        EducationalMaterial(
            id = "edu_healthy_habits_infographic",
            format = EducationalFormat.INFOGRAPHIC,
            topic = EducationalTopic.HEALTHY_HABITS,
            title = "Инфографика: Фундамент здоровых привычек взамен старых ритуалов",
            durationOrReadTime = "Инфографика • 5 мин",
            summary = "Природа не терпит пустоты: чем заполнить вечернее время, чтобы сон был крепким, а нервная система — спокойной.",
            bodyParagraphs = listOf(
                "Мозг привыкает не только к веществу, но и к вечернему ритуалу «переключения после дня». Чтобы трезвость стала легкой и естественной, важно создать новый приятный ритуал в те же часы.",
                "Сочетание умеренной ходьбы на свежем воздухе, теплого травяного чая (иван-чай, мелисса, шиповник), приема магния и чтения или рукоделия полностью перестраивает дофаминовую карту вечера за 21–30 дней."
            ),
            keyScientificFacts = listOf(
                "30 минут спокойной ходьбы при дневном свете повышают выработку собственного мелатонина ночью на 35%.",
                "Контрастный или теплый душ вечером снижает уровень мышечного зажима и кортизола."
            ),
            infographicMetrics = listOf(
                InfographicMetric(
                    label = "Ежедневная прогулка 30–40 минут",
                    valueText = "+45% к качеству сна",
                    fraction = 0.85f,
                    commentary = "Естественная выработка эндорфинов и насыщение мозга кислородом"
                ),
                InfographicMetric(
                    label = "Регулярное трехразовое горячее питание + B1 и Магний",
                    valueText = "-60% риск вечерней тяги",
                    fraction = 0.90f,
                    commentary = "Стабильный сахар в крови исключает ложные приступы желания выпить"
                ),
                InfographicMetric(
                    label = "Отход ко сну в одно и то же время (до 23:00)",
                    valueText = "+50% к восстановлению ЦНС",
                    fraction = 0.80f,
                    commentary = "Глимфатическая система очищает мозг от токсинов именно в глубоком сне"
                )
            )
        ),

        // 6. ВОССТАНОВЛЕНИЕ ПОСЛЕ СРЫВА (Видео-лекция + Статья)
        EducationalMaterial(
            id = "edu_relapse_recovery_video",
            format = EducationalFormat.VIDEO,
            topic = EducationalTopic.RELAPSE_RECOVERY,
            title = "Протокол восстановления после срыва: как не превратить оплошность в запой",
            durationOrReadTime = "Видео-разбор + Статья • 7 мин",
            summary = "Что делать, если произошел срыв: научное различие между разовой оплошностью (lapse) и полным рецидивом (relapse).",
            bodyParagraphs = listOf(
                "Самая опасная психологическая ловушка после единичного употребления — это «Эффект нарушения воздержания» (Abstinence Violation Effect). Человек думает: «Ну всё, я сорвался, счетчик обнулился, я безнадежен, теперь можно пить всю неделю». Именно эта мысль, а не первая выпитая доза, превращает ошибку в тяжелый запой!",
                "В современной медицине разграничивают «оплошность» (однократный эпизод) и «рецидив» (возврат к систематическому пьянству). Если вы остановились сразу же в первый день, ваш организм и мозг сохраняют 95% всей проделанной работы по восстановлению!",
                "Алгоритм экстренного выхода: 1) Немедленно вылейте остатки спиртного; 2) Прекратите ругать себя — замените чувство вины холодным анализом причины; 3) Начните детоксикацию водой, электролитами и тиамином; 4) Свяжитесь с врачом или близким человеком."
            ),
            keyScientificFacts = listOf(
                "Остановка в первые 12 часов после оплошности предотвращает развитие тяжелого абстинентного синдрома в 85% случаев.",
                "Анализ триггера срыва в дневнике самочувствия снижает вероятность повторения той же ошибки в 4 раза."
            ),
            videoChapters = listOf(
                VideoChapter(
                    timeCode = "00:00 — 02:15",
                    chapterTitle = "Глава 1. Ловушка «Сгорел сарай — гори и хата»",
                    slideSummary = "Как чувство вины после одной рюмки толкает человека продолжать пить и как мгновенно выключить самобичевание."
                ),
                VideoChapter(
                    timeCode = "02:15 — 04:40",
                    chapterTitle = "Глава 2. Экстренная физиологическая защита в первые сутки",
                    slideSummary = "Водно-солевой режим, защита сердца и печени, почему категорически нельзя «опохмеляться»."
                ),
                VideoChapter(
                    timeCode = "04:40 — 07:00",
                    chapterTitle = "Глава 3. Работа над ошибками и укрепление брони",
                    slideSummary = "Разбор цепочки событий за 48 часов до срыва: какой сигнал усталости или тревоги был пропущен."
                )
            )
        )
    )

    fun buildAchievementsList(
        soberDays: Int,
        completedMeditationsCount: Int,
        studiedMaterialsCount: Int,
        communityInteractionsCount: Int,
        diaryAndVitalsCount: Int
    ): List<AchievementMilestone> = listOf(
        AchievementMilestone(
            id = "ach_24_hours",
            title = "Первый трезвый рассвет (24 часа)",
            description = "Выдержать первые сутки полной трезвости и запустить очищение организма",
            rewardTitle = "Бронзовый знак «Решимость»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 1,
            categoryLabel = "Вехи трезвости"
        ),
        AchievementMilestone(
            id = "ach_7_days",
            title = "7 дней трезвости",
            description = "Целая неделя без алкоголя: восстановление сна, нормализация вязкости крови и сосудов",
            rewardTitle = "Серебряный щит «Первая Неделя»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 7,
            categoryLabel = "Вехи трезвости"
        ),
        AchievementMilestone(
            id = "ach_14_days",
            title = "14 дней трезвости",
            description = "Две недели чистоты: активное обновление клеток печени и прояснение памяти",
            rewardTitle = "Орден «Ясный Горизонт»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 14,
            categoryLabel = "Вехи трезвости"
        ),
        AchievementMilestone(
            id = "ach_30_days",
            title = "30 дней трезвости",
            description = "Месяц свободной жизни: глубокая регенерация нервной системы и гордость близких",
            rewardTitle = "Золотой венец «Месяц Силы»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 30,
            categoryLabel = "Вехи трезвости"
        ),
        AchievementMilestone(
            id = "ach_first_community_post",
            title = "Первое сообщение в сообществе",
            description = "Написать слова поддержки в ленту сообщества или поддержать других участников",
            rewardTitle = "Знак «Плечо Друга»",
            currentValue = communityInteractionsCount,
            targetValue = 1,
            categoryLabel = "Социальная поддержка"
        ),
        AchievementMilestone(
            id = "ach_5_meditations",
            title = "Завершено 5 медитаций и практик",
            description = "Выполнить 5 дыхательных медитаций 4-7-8, практик НЛП «Взмах» или упражнений КПТ",
            rewardTitle = "Медаль «Мастер Самообладания»",
            currentValue = completedMeditationsCount,
            targetValue = 5,
            categoryLabel = "Психология и практики"
        ),
        AchievementMilestone(
            id = "ach_3_education_studied",
            title = "Вооружен наукой (3 материала)",
            description = "Изучить 3 образовательных материала (статьи, видео-лекции или инфографику)",
            rewardTitle = "Знак «Мудрость и Знание»",
            currentValue = studiedMaterialsCount,
            targetValue = 3,
            categoryLabel = "Образование"
        ),
        AchievementMilestone(
            id = "ach_diary_keeper",
            title = "Дневник здоровья (5 записей)",
            description = "Регулярное ведение зашифрованного дневника самочувствия и замеров пульса/стресса",
            rewardTitle = "Знак «Хранитель Здоровья»",
            currentValue = diaryAndVitalsCount,
            targetValue = 5,
            categoryLabel = "Самоконтроль"
        )
    )
}
