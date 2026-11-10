package com.example.data

import com.example.data.local.EmotionalState
import com.example.data.local.MotivationCategory
import com.example.data.local.TherapyToneMode

data class MotivationalItem(
    val id: String,
    val category: MotivationCategory,
    val title: String,
    val authorOrHero: String,
    val authorRoleOrAge: String,
    val content: String,
    val practicalTakeaway: String,
    val minSoberDays: Int,
    val maxSoberDays: Int,
    val targetEmotions: Set<EmotionalState>,
    val toughModeMessage: String? = null
)

data class AdaptedMotivationBundle(
    val dailyMessage: MotivationalItem,
    val famousQuote: MotivationalItem,
    val realStory: MotivationalItem,
    val adaptationReason: String,
    val allMatchedItems: List<MotivationalItem>
)

object AdaptiveMotivationEngine {

    val allItems: List<MotivationalItem> = listOf(
        // ==================== 1. DAILY ENCOURAGING MESSAGES ====================
        MotivationalItem(
            id = "msg_day_1_3_crisis",
            category = MotivationCategory.DAILY_MESSAGE,
            title = "Первые 72 часа: Тело очищается от яда",
            authorOrHero = "Клиническая поддержка «Ясный Берег»",
            authorRoleOrAge = "Этап детоксикации (1–3 день)",
            content = "Сейчас ваш организм выполняет самую тяжелую и благородную работу — выводит остатки ацетальдегида и перестраивает рецепторы мозга. Любая тревога, потливость или раздражение сегодня — это не слабость, а признак того, что вы выздоравливаете. Вам не нужно решать задачи на год вперед. Ваша единственная задача — оставаться трезвым эти 24 часа.",
            practicalTakeaway = "Пейте теплую минеральную воду без газа, примите магний и витамин B1, избегайте споров и ложитесь спать пораньше.",
            minSoberDays = 0,
            maxSoberDays = 4,
            targetEmotions = setOf(EmotionalState.CRISIS, EmotionalState.ANXIOUS, EmotionalState.TIRED),
            toughModeMessage = "Если вы сейчас выпьете хотя бы 50 грамм «для облегчения», вы обнулите все страдания последних часов и загоните сердце в зону риска аритмии. Перетерпите этот пик — через 48 часов физическая ломка отступит."
        ),
        MotivationalItem(
            id = "msg_day_1_7_calm",
            category = MotivationCategory.DAILY_MESSAGE,
            title = "Каждый трезвый рассвет возвращает вам годы жизни",
            authorOrHero = "Ежедневное напутствие",
            authorRoleOrAge = "Первая неделя трезвости",
            content = "Обратите внимание на свое утро: голова становится светлее, уходит утренний стыд и дрожь в руках. Уже на 5–7 день нормализуется вязкость крови, а клетки печени начинают активное деление. Вы делаете лучший подарок своему сердцу, сосудам и близким людям.",
            practicalTakeaway = "Отметьте в дневнике хотя бы одно приятное изменение в теле за сегодня — вкус еды, ясный взгляд или спокойный пульс.",
            minSoberDays = 0,
            maxSoberDays = 10,
            targetEmotions = setOf(EmotionalState.CALM, EmotionalState.PROUD, EmotionalState.LONELY),
            toughModeMessage = "Не позволяйте первой эйфории обмануть вас мыслью «я уже контролирую себя и могу выпить бокал». Именно на 5–7 день ложная уверенность губит большинство попыток."
        ),
        MotivationalItem(
            id = "msg_week_2_4_anxious",
            category = MotivationCategory.DAILY_MESSAGE,
            title = "Ловушка второй недели: почему возникает пустота и как её пройти",
            authorOrHero = "Нейропсихология трезвости",
            authorRoleOrAge = "Этап стабилизации (7–30 день)",
            content = "Когда острая физическая тяжесть позади, мозг сталкивается с временным дефицитом дофамина. Мир может казаться серым, скучным или тревожным. Наука называет это постабстинентной перестройкой. Ваши рецепторы радости заново учатся чувствовать вкус чая, прогулки, разговора с внуками и хорошего сна. Дайте им время.",
            practicalTakeaway = "Не оставайтесь голодным и уставшим (правило HALT). Съешьте плотный обед и выйдите на 20-минутную прогулку при дневном свете.",
            minSoberDays = 5,
            maxSoberDays = 35,
            targetEmotions = setOf(EmotionalState.ANXIOUS, EmotionalState.LONELY, EmotionalState.TIRED, EmotionalState.CRISIS),
            toughModeMessage = "Скука и раздражение на 2–3 неделе — это предсмертный скулеж зависимости. Алкоголь не вернет радость, он лишь даст 20 минут оглушения и неделю тяжелейшей депрессии."
        ),
        MotivationalItem(
            id = "msg_month_1_3_proud",
            category = MotivationCategory.DAILY_MESSAGE,
            title = "Месяц чистой крови и возрожденного достоинства",
            authorOrHero = "Ежедневное напутствие",
            authorRoleOrAge = "Укрепление трезвости (30–90 дней)",
            content = "За месяц без спиртного объем серого вещества в лобных долях мозга начинает измеримо восстанавливаться. Уходит отечность лица, стабилизируется артериальное давление, возвращается глубокая фаза сна. Вы доказали себе главное: вы способны жить, радоваться и справляться с трудностями без химического костыля.",
            practicalTakeaway = "Потратьте часть сэкономленных от алкоголя денег на то, что укрепляет здоровье: качественные витамины, хорошую книгу или подарок семье.",
            minSoberDays = 20,
            maxSoberDays = 120,
            targetEmotions = setOf(EmotionalState.CALM, EmotionalState.PROUD, EmotionalState.TIRED),
            toughModeMessage = "Один «праздничный тост» после месяца трезвости мгновенно включает старые нейронные цепи зависимости на полную мощность. Защищайте свою трезвость как самую дорогую крепость."
        ),
        MotivationalItem(
            id = "msg_crisis_any_day",
            category = MotivationCategory.DAILY_MESSAGE,
            title = "Волна тяги длится всего 15 минут — переждите её на берегу",
            authorOrHero = "Экстренная поддержка",
            authorRoleOrAge = "Для моментов острой тяги и стресса",
            content = "Прямо сейчас в вашем теле говорит не ваша личность, а старый условный рефлекс на стресс или усталость. Физиологически пик острой тяги живет от 10 до 20 минут, после чего неизбежно идет на спад, как морская волна. Выпейте два больших стакана воды, сделайте 10 глубоких вдохов с долгим выдохом и позвольте этой волне разбиться без вашего участия.",
            practicalTakeaway = "Откройте вкладку «Методы» и запустите дыхательный тренажер 4-7-8 или технику НЛП «Взмах» прямо сейчас.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.CRISIS, EmotionalState.ANXIOUS),
            toughModeMessage = "Посмотрите правде в глаза: еще ни одна жизненная проблема за всю историю человечества не стала легче после пьянки. Завтра проблема останется на месте, но к ней добавятся тахикардия, похмельный ужас и отвращение к себе."
        ),
        MotivationalItem(
            id = "msg_long_term_mastery",
            category = MotivationCategory.DAILY_MESSAGE,
            title = "Зрелая трезвость: ваша история уже помогает другим",
            authorOrHero = "Напутствие мастера",
            authorRoleOrAge = "Устойчивая ремиссия (90+ дней)",
            content = "Вы прошли путь, который миллионам людей кажется невозможным. Ваша нервная система обрела устойчивость, печень и сосуды работают в чистом режиме, а ваше слово снова имеет твердый вес. Помогая тем, кто только делает первые шаги в сообществе, вы делаете собственную трезвость несокрушимой.",
            practicalTakeaway = "Загляните в раздел «Сообщество» и оставьте короткое слово поддержки тем, у кого сегодня первая неделя.",
            minSoberDays = 60,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.CALM, EmotionalState.PROUD, EmotionalState.LONELY, EmotionalState.TIRED),
            toughModeMessage = "У зависимостей нет срока давности. Даже через полгода или год первый глоток возвращает человека не к «культурному питию», а в ту же яму, из которой он выбирался с таким трудом."
        ),

        // ==================== 2. FAMOUS PERSONALITIES QUOTES ON SOBRIETY ====================
        MotivationalItem(
            id = "quote_tolstoy_conscience",
            category = MotivationCategory.FAMOUS_QUOTE,
            title = "Одурманивание совести и ясность жизни",
            authorOrHero = "Лев Николаевич Толстой",
            authorRoleOrAge = "Писатель и мыслитель (эссе «Для чего люди одурманиваются?»)",
            content = "«Люди пьют вино не для веселья, а для того, чтобы заглушить голос совести и разума. Стоит только человеку перестать одурманивать себя, как глаза его открываются на истинную жизнь, и он ужасается тому, в каком тумане он жил прежде».",
            practicalTakeaway = "Трезвость возвращает честный контакт с самим собой. Любую трудность легче решить ясным умом, чем прятать в тумане.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.CRISIS, EmotionalState.ANXIOUS, EmotionalState.CALM)
        ),
        MotivationalItem(
            id = "quote_uglov_surgeon",
            category = MotivationCategory.FAMOUS_QUOTE,
            title = "Правда академика, прожившего 103 года в абсолютной трезвости",
            authorOrHero = "Федор Григорьевич Углов",
            authorRoleOrAge = "Академик АМН, легендарный хирург, оперировавший в 100 лет",
            content = "«За всю свою долгую жизнь я не выпил ни одной рюмки спиртного. Как хирург, стоявший у операционного стола больше семидесяти лет, я свидетельствую: нет ни одного органа в теле человека, который бы не разрушался даже от малых доз алкоголя. Трезвость — это естественное, нормальное и радостное состояние здорового человека».",
            practicalTakeaway = "Долголетие, твердая рука и ясная память в преклонном возрасте начинаются с полного отказа от этанола.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.PROUD, EmotionalState.CALM, EmotionalState.TIRED, EmotionalState.CRISIS)
        ),
        MotivationalItem(
            id = "quote_bekhterev_brain",
            category = MotivationCategory.FAMOUS_QUOTE,
            title = "О мнимом «согревании» и параличе воли",
            authorOrHero = "Владимир Михайлович Бехтерев",
            authorRoleOrAge = "Выдающийся русский невролог, психиатр и физиолог мозга",
            content = "«Всякое количество алкоголя, даже самое малое, ослабляет умственную работоспособность, парализует волю и нравственные задержки. Алкоголь обманывает человека: он дает иллюзию тепла, отнимая тепло у тела, и дает иллюзию силы, растрачивая последние резервы нервной системы».",
            practicalTakeaway = "Когда усталость шепчет «выпей для бодрости», помните слова академика Бехтерева: спиртное берет энергию взаймы у вашего завтрашнего дня под огромный процент.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.TIRED, EmotionalState.ANXIOUS, EmotionalState.CRISIS)
        ),
        MotivationalItem(
            id = "quote_hopkins_freedom",
            category = MotivationCategory.FAMOUS_QUOTE,
            title = "Сорок восемь лет трезвости после края пропасти",
            authorOrHero = "Сэр Энтони Хопкинс",
            authorRoleOrAge = "Оскароносный актер (в трезвости с декабря 1975 года)",
            content = "«Когда я наконец попросил о помощи и решил бросить пить, тяга ушла, а на её место пришло невероятное облегчение. Не стыдитесь просить помощи и не бойтесь признать бессилие перед бутылкой. По ту сторону отказа от алкоголя вас ждет жизнь, о красоте которой вы даже не подозревали».",
            practicalTakeaway = "Возраст не помеха для великих перемен: лучшие роли, здоровье и уважение пришли к Хопкинсу именно после полного отказа от спиртного.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.LONELY, EmotionalState.ANXIOUS, EmotionalState.PROUD)
        ),
        MotivationalItem(
            id = "quote_seneca_slavery",
            category = MotivationCategory.FAMOUS_QUOTE,
            title = "О добровольном безумии",
            authorOrHero = "Луций Анней Сенека",
            authorRoleOrAge = "Римский философ-стоик («Нравственные письма к Луцилию»)",
            content = "«Опьянение — это не что иное, как добровольное безумие. Протяни оно несколько дней подряд — и никто не усомнится, что человек сошел с ума. Назови мне хоть одного мудрого человека, который предпочел бы рабство у кубка свободе своего духа».",
            practicalTakeaway = "Стоическая формула проста: между импульсом (желанием) и действием всегда есть пауза. В этой паузе живет ваша свобода.",
            minSoberDays = 3,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.CRISIS, EmotionalState.CALM, EmotionalState.PROUD)
        ),
        MotivationalItem(
            id = "quote_jack_london",
            category = MotivationCategory.FAMOUS_QUOTE,
            title = "Коварство «Джона Ячменное Зерно»",
            authorOrHero = "Джек Лондон",
            authorRoleOrAge = "Писатель (автобиографическая повесть об алкоголе)",
            content = "«Алкоголь говорит с человеком на языке ложной дружбы: сначала он обещает товарищество и широту души, а затем незаметно крадет здоровье, ясность мысли и саму радость бытия, требуя взамен всё новую дань».",
            practicalTakeaway = "Распознавайте голос зависимости как голос лжеца — не вступайте с ним в переговоры.",
            minSoberDays = 1,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.LONELY, EmotionalState.CRISIS, EmotionalState.ANXIOUS)
        ),

        // ==================== 3. REAL-LIFE SUCCESS STORIES ====================
        MotivationalItem(
            id = "story_viktor_64_engineer",
            category = MotivationCategory.REAL_STORY,
            title = "«В 62 года врачи дали мне полгода. Сегодня я учу внука мастерить лодку»",
            authorOrHero = "Виктор Павлович, 64 года",
            authorRoleOrAge = "Инженер-механик на пенсии, трезв 2 года и 4 месяца",
            content = "«После выхода на пенсию и потери супруги я начал выпивать каждый вечер — сначала „по сто грамм от тоски и для сна“, а через год уже не мог встать с постели без опохмелки. Давление подскакивало до 190/110, пульс колотился под 115, начались провалы в памяти и сильная боль в правом боку. Однажды приехала дочь с семилетним внуком, а я не смог даже подняться с пола. Глаза внука я запомнил на всю жизнь.\n\nВрач-нарколог сказал мне прямо и жестко: „Виктор Павлович, еще один запой — и будет инсульт, будете лежать парализованным на руках у дочери“. Эта жесткая правда отрезвила меня лучше любых уговоров.\n\nПервые две недели я буквально держался за расписание по часам: пил тиамин B1, магний, вел бумажный дневник давления и каждый вечер, когда накатывала тоска, выходил быстрым шагом в парк с термосом шиповника. Через месяц ушла дрожь в пальцах, через три месяца печеночные пробы (АЛТ и АСТ) вернулись в норму! Сейчас мне 64, давление 125/80, мы с внуком каждые выходные строгаем в мастерской. Жизнь на пенсии только началась».",
            practicalTakeaway = "Секрет Виктора Павловича: не оставаться вечером в четырех стенах наедине с телевизором — заменить старый вечерний ритуал прогулкой и травяным чаем в термосе.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.LONELY, EmotionalState.CRISIS, EmotionalState.TIRED, EmotionalState.ANXIOUS)
        ),
        MotivationalItem(
            id = "story_elena_58_teacher",
            category = MotivationCategory.REAL_STORY,
            title = "«Тихий вечерний алкоголизм чуть не разрушил мою семью и здоровье»",
            authorOrHero = "Елена Сергеевна, 58 лет",
            authorRoleOrAge = "Преподаватель математики, трезва 1 год и 7 месяцев",
            content = "«Я никогда не валялась под забором — я пила „культурно“, дома на кухне после тяжелого дня. Сначала бокал вина, потом бутылка, потом коньяк, чтобы уснуть. По утрам лицо отекало, сердце выскакивало из груди, на уроках я стала срываться на детей и забывать простые слова. Самым страшным было чувство вины каждое утро в 4 часа ночи, когда просыпаешься в холодном поту.\n\nМне помогла техника когнитивного дневника и правило „Не пить только сегодня“. Когда вечером мозг шептал: „Ты устала, купи одну маленькую“, я включала жесткую перемотку вперед: детально представляла свое завтрашнее опухшее лицо, запах перегара и стыд перед детьми. Дополнительно по совету врача пропила курс NAC и L-теанина — ушла вечерняя взвинченность.\n\nЧерез 40 дней трезвости я впервые за 10 лет проспала всю ночь без пробуждений и проснулась с ощущением чистой детской радости. Дети снова тянутся ко мне, а я помолодела минимум на восемь лет».",
            practicalTakeaway = "Метод Елены Сергеевны: техника «Перемотка пленки до утра» — никогда не думать о первых 15 минутах опьянения, а сразу вспоминать состояние в 4 часа утра.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.ANXIOUS, EmotionalState.TIRED, EmotionalState.CALM)
        ),
        MotivationalItem(
            id = "story_nikolay_68_carpenter",
            category = MotivationCategory.REAL_STORY,
            title = "«Пил 35 лет, думал в моем возрасте уже не бросают. Оказалось — еще как!»",
            authorOrHero = "Николай Иванович, 68 лет",
            authorRoleOrAge = "Мастер-краснодеревщик, трезв 3 года и 1 месяц",
            content = "«Я всю жизнь работал руками и считал, что пятница и суббота без бутылки — не выходные. К 65 годам ноги начали неметь от алкогольной полинейропатии, сон пропал совсем, а кардиолог показал мне кардиограмму с мерцательной аритмией. Я думал: „Ну сколько мне осталось, поздно уже меняться“.\n\nСын настроил мне дневник самочувствия и подарил простой пульсометр. Когда я своими глазами увидел на графике, что после любой рюмки мой пульс ночью держится 102 удара в минуту и сердце буквально изнашивается без отдыха, а в трезвые дни пульс падает до спокойных 64 ударов — во мне щелкнул тумблер.\n\nПроколол витамины группы B, начал делать дыхательные упражнения при каждом приступе раздражения. Через полгода чувствительность в ступнях вернулась, руки снова слушаются миллиметр в миллиметр. В 68 лет я чувствую себя крепче, чем в 50!»",
            practicalTakeaway = "Опыт Николая Ивановича: цифры пульса и давления не врут. Следите за объективными показателями сердца — они наглядно показывают благодарность организма за каждый трезвый день.",
            minSoberDays = 0,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.PROUD, EmotionalState.CALM, EmotionalState.TIRED, EmotionalState.CRISIS)
        ),
        MotivationalItem(
            id = "story_sergey_49_driver",
            category = MotivationCategory.REAL_STORY,
            title = "«Как я пережил срыв на 45-й день и больше не возвращался к стакану»",
            authorOrHero = "Сергей, 49 лет",
            authorRoleOrAge = "Водитель-экспедитор, трезв 1 год и 2 месяца",
            content = "«Первую попытку бросить я сорвал на 45-й день на дне рождения старого друга. Мне казалось: „Я уже полтора месяца не пью, значит вылечился, от одной рюмки ничего не будет“. Эта одна рюмка превратилась в десятидневный кошмар, потерю работы и больничную капельницу.\n\nТогда я понял главное правило науки о зависимости: в мозге зависимого человека навсегда остается „соленый огурец“, который никогда снова не станет свежим огурцом. Единственная рюмка, которую я действительно могу контролировать — это ПЕРВАЯ рюмка. Пока я не выпил первую — я полностью хозяин своей жизни.\n\nЯ использовал технику НЛП с якорением: на каждую мысль о спиртном жестко привязал запах нашатыря и воспоминание о больничном потолке. Вот уже больше года я спокоен на любом застолье, вернул права, уважение жены и купил новую машину на деньги, которые раньше уходили в алкомаркет».",
            practicalTakeaway = "Железное правило Сергея: вы не обязаны бороться с десятой рюмкой — достаточно никогда не прикасаться к первой.",
            minSoberDays = 10,
            maxSoberDays = 3650,
            targetEmotions = setOf(EmotionalState.CRISIS, EmotionalState.PROUD, EmotionalState.CALM, EmotionalState.ANXIOUS)
        )
    )

    fun selectAdaptedBundle(
        soberDays: Int,
        currentEmotion: EmotionalState,
        cravingLevel: Int,
        stressIndex: Int,
        toneMode: TherapyToneMode
    ): AdaptedMotivationBundle {
        val effectiveEmotion = when {
            cravingLevel >= 7 || stressIndex >= 75 -> EmotionalState.CRISIS
            cravingLevel >= 5 || stressIndex >= 60 -> EmotionalState.ANXIOUS
            else -> currentEmotion
        }

        fun scoreItem(item: MotivationalItem): Int {
            var score = 0
            if (soberDays in item.minSoberDays..item.maxSoberDays) {
                score += 50
            } else {
                val dist = minOf(
                    kotlin.math.abs(soberDays - item.minSoberDays),
                    kotlin.math.abs(soberDays - item.maxSoberDays)
                )
                score += (30 - dist).coerceAtLeast(0)
            }
            if (item.targetEmotions.contains(effectiveEmotion)) {
                score += 45
            }
            if (toneMode == TherapyToneMode.TOUGH && item.toughModeMessage != null) {
                score += 15
            }
            return score
        }

        val dailyMessages = allItems
            .filter { it.category == MotivationCategory.DAILY_MESSAGE }
            .sortedByDescending { scoreItem(it) }

        val quotes = allItems
            .filter { it.category == MotivationCategory.FAMOUS_QUOTE }
            .sortedByDescending { scoreItem(it) }

        val stories = allItems
            .filter { it.category == MotivationCategory.REAL_STORY }
            .sortedByDescending { scoreItem(it) }

        val stageText = when {
            soberDays <= 3 -> "острый период детоксикации ($soberDays дн.)"
            soberDays <= 14 -> "этап ранней стабилизации ($soberDays дн.)"
            soberDays <= 60 -> "формирование устойчивой трезвости ($soberDays дн.)"
            else -> "уверенная ремиссия ($soberDays дн.)"
        }

        val reason = "Адаптировано под ваш прогресс: $stageText • Эмоциональный фон: «${effectiveEmotion.titleRu}» (Тяга: $cravingLevel/10, Стресс: $stressIndex%)"

        return AdaptedMotivationBundle(
            dailyMessage = dailyMessages.first(),
            famousQuote = quotes.first(),
            realStory = stories.first(),
            adaptationReason = reason,
            allMatchedItems = (dailyMessages + quotes + stories).sortedByDescending { scoreItem(it) }
        )
    }
}
