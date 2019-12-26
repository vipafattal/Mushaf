package co.jp.smagroup.musahaf.framework.data.local

import co.jp.smagroup.musahaf.framework.api.ApiModels
import co.jp.smagroup.musahaf.framework.commen.MusahafConstants
import co.jp.smagroup.musahaf.model.*
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import com.codebox.lib.extrenalLib.TinyDB
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.raizlabs.android.dbflow.kotlinextensions.and
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.raizlabs.android.dbflow.kotlinextensions.notEq
import javax.inject.Inject

/**
 * Created by ${User} on ${Date}
 */

class LocalDataSource : LocalDataSourceProviders {
    private val sajdaList: List<Int> =
        listOf(1160, 1722, 1951, 2138, 2308, 2613, 2672, 2915, 3185, 3518, 3994, 4256, 4846, 5905, 6125)
    @Inject
    lateinit var tinyDB: TinyDB

    init {
        MushafApplication.appComponent.inject(this)
    }

    override suspend fun addSupportedLanguages(languages: List<String>) {
        withContext(Dispatchers.Main) {
            tinyDB.putListString(PreferencesConstants.SupportedLanguage, languages)
        }
    }

    override suspend fun addAyat(
        quran: ApiModels.QuranDataApi
    ) {
        for (aya in quran.ayahs) Aya(aya, quran.edition).save()
    }

    override suspend fun addEditions(editions: List<Edition>) {
        for (edi in editions) edi.save()
    }

    override suspend fun addEdition(edition: Edition) {
        edition.save()
    }

    override suspend fun addDownloadingState(downloadingState: DownloadingState) {
        downloadingState.save()
    }

    override suspend fun addDownloadReciter(reciters: List<Reciter>) {
        for (rec in reciters)
            rec.save()
    }

    override fun addDownloadReciter(reciter: Reciter) {
        reciter.save()
    }

    override suspend fun getSupportedLanguage(): List<String> =
        withContext(Dispatchers.Main) {
            tinyDB.getListString(PreferencesConstants.SupportedLanguage)
        }


    override fun updateBookmarkStatus(ayaNumber: Int, identifier: String, bookmarkStatus: Boolean) {
        SQLite.update(Aya::class.java)
            .set(Aya_Table.isBookmarked.eq(bookmarkStatus))
            .where(Aya_Table.edition_identifier.`is`(identifier).and(Aya_Table.number eq ayaNumber)).async().execute()
    }

    override suspend fun searchTranslation(query: String, type: String): MutableList<Aya> {
        return SQLite.select().from(Aya::class.java).innerJoin(Edition::class.java)
            .on(Edition_Table.identifier.`is`(Aya_Table.edition_identifier)).where(Edition_Table.type eq type)
            .and(Aya_Table.text.like("%$query%")).queryList()
    }


    override suspend fun getAvailableEditions(format: String, language: String): List<Edition> =
        SQLite.select().from(Edition::class.java)
            .where((Edition_Table.format eq format) and (Edition_Table.language eq language) and (Edition_Table.identifier notEq MusahafConstants.MainMusahaf))
            .queryList()

    override suspend fun getAllEditions(): List<Edition> =
        SQLite.select().from(Edition::class.java).queryList()

    override fun getEditionsByType(type: String): List<Edition> =
        SQLite.select().from(Edition::class.java).where(Edition_Table.format eq type).queryList()

    override suspend fun getAvailableReciters(): List<Edition> =
        SQLite.select().from(Edition::class.java).where(Edition_Table.format eq MusahafConstants.Audio).queryList()
    override suspend fun getDownloadingState(identifier: String): DownloadingState =
        SQLite.select().from(DownloadingState::class.java).where(DownloadingState_Table.identifier eq identifier).querySingle()
            ?: DownloadingState(identifier = identifier)

    override suspend fun getAllAyatByIdentifier(musahafIdentifier: String): MutableList<Aya> {
        return SQLite.select().from(Aya::class.java)
            .where(Aya_Table.edition_identifier eq musahafIdentifier)
            .orderBy(Aya_Table.number, true)
            .queryList()
    }

    override suspend fun getPage(musahafIdentifier: String, page: Int): MutableList<Aya> {
        return SQLite.select().from(Aya::class.java)
            .where(Aya_Table.page eq page)
            .and(Aya_Table.edition_identifier eq musahafIdentifier)
            .orderBy(Aya_Table.numberInSurah, true)
            .queryList()
    }

    override suspend fun getQuranBySurah(musahafIdentifier: String, surahNumber: Int): MutableList<Aya> {
        return SQLite.select().from(Aya::class.java)
            .where(Aya_Table.edition_identifier eq musahafIdentifier)
            .and(Aya_Table.surah_number eq surahNumber)
            .queryList()
    }

    override suspend fun getAyaByNumberInMusahaf(musahafIdentifier: String, number: Int): Aya {
        val aya = SQLite.select().from(Aya::class.java)
            .where(Aya_Table.edition_identifier eq musahafIdentifier)
            .and(Aya_Table.number eq number)
            .querySingle() ?: throw IllegalStateException("$musahafIdentifier at aya $number is not downloaded")
        return aya
    }

    override suspend fun getAllByBookmarkStatus(bookmarkStatus: Boolean): MutableList<Aya> =
        SQLite.select().from(Aya::class.java)
            .where(Aya_Table.isBookmarked eq bookmarkStatus)
            .queryList()


    override suspend fun getByAyaByBookmark(editionIdentifier: String, bookmarkStatus: Boolean): MutableList<Aya> =
        SQLite.select().from(Aya::class.java)
            .where(Aya_Table.edition_identifier eq editionIdentifier)
            .and(Aya_Table.isBookmarked eq bookmarkStatus)
            .queryList()


    override suspend fun getAyatByRange(from: Int, to: Int): MutableList<Aya> =
        SQLite.select().from(Aya::class.java)
            .where(Aya_Table.edition_identifier eq MusahafConstants.MainMusahaf)
            .and(Aya_Table.number.greaterThanOrEq(from) and Aya_Table.number.lessThanOrEq(to))
            .queryList()


    override suspend fun getReciterDownload(ayaNumber: Int, reciterIdentifier: String): Reciter? =
        SQLite.select().from(Reciter::class.java)
            .where(Reciter_Table.number eq ayaNumber)
            .and(Reciter_Table.identifier eq reciterIdentifier).querySingle()

    override fun getReciterDownloads(from: Int, to: Int, reciterIdentifier: String): List<Reciter> =
        SQLite.select().from(Reciter::class.java)
            .where(Reciter_Table.number.greaterThanOrEq(from) and Reciter_Table.number.lessThanOrEq(to))
            .and(Reciter_Table.name eq reciterIdentifier).queryList()

    override suspend fun getDownloadedDataReciter(reciterName: String): List<Reciter> =
        SQLite.select().from(Reciter::class.java)
            .where(Reciter_Table.name eq reciterName).queryList()

}