package com.example.composetdaapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.composetdaapp.data.entities.account.Accounts
import com.example.composetdaapp.data.entities.account.Positions
import com.example.composetdaapp.data.entities.quotes.SymbolDetails
import com.example.composetdaapp.data.entities.watchlist.Watchlist
import com.example.composetdaapp.data.entities.websocket.response.Content
import com.example.composetdaapp.data.entities.websocket.response.DataResponse
import com.example.composetdaapp.indicators.UpperIndicators
import com.example.composetdaapp.utils.MyPreference
import com.example.composetdaapp.utils.Resource
import com.example.composetdaapp.data.api.MainRepository
import com.example.composetdaapp.data.entities.orders.get.GetOrderItem

import com.example.composetdaapp.utils.SocketInteractor
import com.github.mikephil.charting.data.Entry
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext


@Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val interactor: SocketInteractor,
    private val moshi: Moshi,
    private val repository: MainRepository,

) :
    ViewModel() {

    /*Start of Jeptpack state*/
    private val _cards = MutableStateFlow(listOf<GetOrderItem>())
    val cards: StateFlow<List<GetOrderItem>> get() = _cards

    private val _expandedCardIdsList = MutableStateFlow(listOf<Int>())
    val expandedCardIdsList: StateFlow<List<Int>> get() = _expandedCardIdsList


    fun getOrders() {
        scope.launch() {
            val orders = repository.getOrders()
            println("BODY ERRO: " + orders)
            orders.data?.let { _cards.emit(it) }
            ordersLiveData.postValue(orders)
        }

    }

    fun onCardArrowClicked(cardId: Int?) {
        _expandedCardIdsList.value = _expandedCardIdsList.value.toMutableList().also { list ->
            if (list.contains(cardId)) list.remove(cardId) else list.add(cardId!!)
        }
    }


    /* End of Jetpack*/


    //ADD RESOURCE TO ALL VAL/VAR


    //create the job, which implements coroutines context.
    var job = Job()

    //create the coroutine context with the job and the dispatcher(identifies the Thread that will be use, need MAIN for mediatorlivedata)
    private val coroutineContext: CoroutineContext get() = job + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    private val _symbol = MutableLiveData<String>()

    val watchlistLiveData = MutableLiveData<Resource<List<Watchlist>>>()

    val watchlistQuotes = MutableLiveData<Resource<MutableMap<String?, SymbolDetails>>>()

    var accountDetailsLiveData = MutableLiveData<Resource<Accounts>>()

    var posQuotesLiveData = MutableLiveData<List<SymbolDetails>>()

    var posMediatorLiveData = MediatorLiveData<MutableMap<Positions, SymbolDetails>>()

    val watchlistNames: MutableList<String> = ArrayList()

    private val symbolLiveData = MutableLiveData<Resource<MutableMap<String?, SymbolDetails>>>()

    val ordersLiveData = MutableLiveData<Resource<List<GetOrderItem>>>()

    var upperIndicators = UpperIndicators()

    var indicatorLiveData: MutableLiveData<List<Entry>> = MutableLiveData()

    val webSocketLiveData = MutableLiveData<MutableMap<String, Content>>()

//TODO add indicators once we fix intra-day graphs
    /*private suspend fun getIndicatorData(historicalData: Resource<HistoricalData>): List<Entry> {
       val results = CompletableDeferred<List<Entry>>()
       withContext(Dispatchers.IO) {
           results.complete(
               upperIndicators.simpleMovingAverage(
                   historicalData,
                   50,
                   candleEntries.size
               )
           )
       }
       return results.await()
   }*/

    /*private suspend fun getIndicatorData(historicalData: Resource<HistoricalData>): List<Entry> {
     val results = CompletableDeferred<List<Entry>>()
     withContext(Dispatchers.IO) {
         results.complete(
             upperIndicators.simpleMovingAverage(
                 historicalData,
                 50,
                 candleEntries.size
             )
         )
     }
     return results.await()
 }*/



    //Positions and Quotes together
    fun accountPosDetails() {
        scope.launch {
            val accDetails = getAccountDetails()
            accountDetailsLiveData.postValue(accDetails)
            val accPositions = (accDetails.data?.securitiesAccount?.positions)
            val stringBuilder = StringBuilder()
            if (!accPositions.isNullOrEmpty()) {
                accPositions.removeIf { it.instrument.symbol == "MMDA1" }
                for (i in accPositions.indices) {
                    stringBuilder.append(accPositions[i].instrument.symbol + ",")
                }
            }
            //HTTP request for Positions Quotes
            val posQuotes = repository.getSymbolDetails(stringBuilder.toString())
            if (!posQuotes.data.isNullOrEmpty()) {
                posQuotesLiveData.postValue(posQuotes.data.values.toMutableList())
            }

            try {
                //observe positions update
                posMediatorLiveData.addSource(accountDetailsLiveData) { posValues ->
                    if (!posQuotesLiveData.value.isNullOrEmpty()) {
                        when (posValues.status) {
                            Resource.Status.SUCCESS -> {
                                val accPosUpdate = mutableMapOf<Positions, SymbolDetails>()
                                if (!accPositions.isNullOrEmpty()) {
                                    for (i in accPositions.indices) {
                                        //after selling a position this crahses due to a index out of bounds
                                        accPosUpdate[posValues.data!!.securitiesAccount.positions[i]] =
                                            posQuotesLiveData.value!![i]
                                    }
                                }
                                posMediatorLiveData.postValue(accPosUpdate)
                            }
                            Resource.Status.ERROR -> Timber.e(posValues.message)
                            Resource.Status.LOADING -> Timber.v("Loading")
                        }
                    }
                }
                //observer quote
                posMediatorLiveData.addSource(posQuotesLiveData) { quoteValues ->
                    val accQuotesUpdate = mutableMapOf<Positions, SymbolDetails>()
                    for (i in accPositions!!.indices) {
                        accQuotesUpdate[accountDetailsLiveData.value!!.data!!.securitiesAccount.positions[i]] =
                            quoteValues[i]
                    }
                    posMediatorLiveData.postValue(accQuotesUpdate)

                }
            } catch (e: IllegalArgumentException) {
                posMediatorLiveData.removeSource(symbolLiveData)
                posMediatorLiveData.removeSource(posQuotesLiveData)
            }
        }
    }


    private suspend fun getAccountDetails() = repository.getAccountDetails()


    fun getAllWatchlist() {
        scope.launch {
            val allWatchlist = repository.getAllWatchlist()
            if (!allWatchlist.data.isNullOrEmpty()) {
                watchlistNames.clear()
                for (i in allWatchlist.data.indices) {
                    watchlistNames.add(i, allWatchlist.data[i].name)
                }
            }
            watchlistLiveData.postValue(allWatchlist)
        }
    }

    fun patchWatchlist(watchlistId: String, symbolUpdate: String) {
        scope.launch {
            val patchedWatchlists =
                repository.patchWatchlist("Account Number", watchlistId, symbolUpdate)
        }
    }

    fun getMultiSymbolDetails(string: String) {
        scope.launch {
            val multiSymbolDetails = repository.getSymbolDetails(string)
            watchlistQuotes.postValue(multiSymbolDetails)
        }
    }


    fun start(symbol: String) {
        _symbol.value = symbol
    }

    //TODO Create data classes for requests
    val levelOneFutures = "{\n" +
            "            \"service\": \"LEVELONE_FUTURES\",\n" +
            "            \"requestid\": \"1\",\n" +
            "            \"command\": \"SUBS\",\n" +
            "            \"account\": \"Account #\",\n" +
            "            \"source\": \"gerardoiza94\",\n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"/ES,/NQ,/YM\",\n" +
            "                \"fields\": \"0,3,19,20,34\"\n" +
            "            }\n" +
            "        }"

    val stockQoutes = "{\n" +
            "            \"service\": \"QOUTE\",\n" +
            "            \"requestid\": \"1\",\n" +
            "            \"command\": \"SUBS\",\n" +
            "            \"account\": \"Account\",\n" +
            "            \"source\": \"UserId\",\n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"amd\",\n" +
            "                \"fields\": \"0,3,19,20,34\"\n" +
            "            }\n" +
            "        }"

    val levelOneOptions = "{\n" +
            "            \"service\": \"OPTIONS_BOOK\",\n" +
            "            \"requestid\": \"1\",\n" +
            "            \"command\": \"SUBS\",\n" +
            "            \"account\": \"Account\",\n" +
            "            \"source\": \"UserId\",\n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"SPY_041822P400\",\n" +
            "                \"fields\": \"0,3,19,20,34\"\n" +
            "            }\n" +
            "        }"

    val futuresCandle = "{\n" +
            "            \"service\": \"CHART_FUTURES\",\n" +
            "            \"requestid\": \"1\",\n" +
            "            \"command\": \"SUBS\",\n" +
            "            \"account\": \"Account\",\n" +
            "            \"source\": \"UserId\",\n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"/ES\",\n" +
            "                \"fields\": \"0,1,2,3,4,5,6,7\"\n" +
            "            }\n" +
            "        }"

    val futuresHistory = " {\n" +
            "            \"service\": \"CHART_HISTORY_FUTURES\",\n" +
            "            \"requestid\": \"2\",\n" +
            "            \"command\": \"GET\",\n" +
            "            \"account\": \"Account\",\n" +
            "            \"source\": \"UserId\",\n" +
            "            \"parameters\": {\n" +
            "                \"symbol\": \"/ES\",\n" +
            "                \"frequency\": \"m1\",\n" +
            "                \"period\": \"d1\"\n" +
            "            }\n" +
            "        }"


    val accActivity = "{\n" +
            "            \"service\": \"ACCT_ACTIVITY\", \n" +
            "            \"requestid\": \"2\", \n" +
            "            \"command\": \"SUBS\", \n" +
            "            \"account\": \"Account\", \n" +
            "            \"source\": \"UserId\", \n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"b71f01142692445eca51554fea6789343cf24399dc98b4f638d8611b9a4bcda91a3519298a2d7784c976c7ec555cb02ef\", \n" +
            "                \"fields\": \"0,1,2,3\"\n" +
            "            }\n" +
            "        }"


    val newsHeadline = "  {\n" +
            "            \"service\": \"NEWS_HEADLINE\", \n" +
            "            \"requestid\": \"2\", \n" +
            "            \"command\": \"SUBS\", \n" +
            "            \"account\": \" Account \", \n" +
            "            \"source\": \"UserId\", \n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"GOOG\", \n" +
            "                \"fields\": \"0,1,2,3,4,5,6\"\n" +
            "            }\n" +
            "        }"


    val listedBook = "  {\n" +
            "            \"service\": \"NASDAQ_BOOK\", \n" +
            "            \"requestid\": \"2\", \n" +
            "            \"command\": \"SUBS\", \n" +
            "            \"account\": \" Account \", \n" +
            "            \"source\": \"UserId\", \n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"SPY\", \n" +
            "                \"fields\": \"0,1,2,3,4\"\n" +
            "            }\n" +
            "        }"

    val levelONeForex = "  {\n" +
            "            \"service\": \"LEVELONE_FOREX\",\n" +
            "            \"requestid\": \"2\",\n" +
            "            \"command\": \"SUBS\",\n" +
            "            \"account\": \"Account\",\n" +
            "            \"source\": \"UserId\",\n" +
            "            \"parameters\": {\n" +
            "                \"keys\": \"EUR/USD\",\n" +
            "                \"fields\": \"0,1,2,3,4,5,6,7,8,9,10,11,12,13\"\n" +
            "            }\n" +
            "        }"

    val leveloneFuturesTime =
            "        {\n" +
            "            \"service\": \"CHART_HISTORY_FUTURES\",\n" +
            "            \"requestid\": \"2\",\n" +
            "            \"command\": \"GET\",\n" +
            "            \"account\": \"Account\",\n" +
            "            \"source\": \"UserId\",\n" +
            "            \"parameters\": {\n" +
            "                \"symbol\": \"/ES\",\n" +
            "                \"END_TIME\": \"1649104859000\",\n" +
            "                \"START_TIME\": \"1648840881000\",\n" +
            "                \"frequency\": \"h2\"\n" +

            "            }\n" +
            "        }\n"


    private fun sendFuturesPayload() = interactor.sendSocketRequest(levelOneFutures)




    @ExperimentalCoroutinesApi
    fun subscribeToSocketEvents() {
        viewModelScope.launch {
            try {
                //TODO Create a data class and custom deserializer
                interactor.startSocket().consumeEach {
                    if (it.exception == null) {
                        val jsonObject = JSONObject(it.text.toString())
                        //filter response by "data" refactor as a util
                        if (jsonObject.has("data")) {
                            val jsonAdapter = moshi.adapter(DataResponse::class.java)

                            val dataResponse = jsonAdapter.fromJson(it.text.toString())
                            val dataMap = mutableMapOf<String, Content>()
                            fun <T> MutableLiveData<T>.notifyObserver() {
                                this.value = this.value
                            }
                            //Make Dictionary.
                            if (dataResponse != null) {
                                for (i in dataResponse.data[0].content.indices) {
                                    dataMap[dataResponse.data[0].content[i].key] =
                                        dataResponse.data[0].content[i]
                                }
                                if (webSocketLiveData.value.isNullOrEmpty()) {
                                    //websockets only updates symbols changed, post value would replace all 3 symbols with the new data.
                                    webSocketLiveData.postValue(dataMap)
                                } else {
                                    //we use putALl to only update the future symbol that changed and notifyObserver manually (postValue does it auto).
                                    webSocketLiveData.value!!.putAll(dataMap)
                                    webSocketLiveData.notifyObserver()
                                }
                            }
                        }
                        if (jsonObject.has("response")) {
                            val data = jsonObject.getJSONArray("response")
                            val content = data.getJSONObject(0)
                            if (content.getString("command") == "LOGIN") {
                                println("Login Command Successful")
                                //start data subscriptions
                                sendFuturesPayload()
                            }
                        }
                        if (jsonObject.has("snapshot")) {
                            val jsonAdapter = moshi.adapter(DataResponse::class.java)

                            val dataResponse = jsonAdapter.fromJson(it.text.toString())
                            println("WEBSOCKET RESPONSE 1" + dataResponse!!.data.lastIndex)


                        }
                    } else {
                        onSocketError(it.exception)
                    }
                }
            } catch (ex: java.lang.Exception) {
                onSocketError(ex)
            }
        }
    }

    private fun onSocketError(ex: Throwable) {
        println("Error occurred : ${ex.message}")
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        interactor.stopSocket()
        super.onCleared()
    }


}






