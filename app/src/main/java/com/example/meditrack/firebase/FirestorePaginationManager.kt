package com.example.meditrack.firebase

import com.example.meditrack.dataModel.dataClasses.SessData2
import com.example.meditrack.dataModel.dataClasses.SessData2.Companion.toUserSessionData
import com.example.meditrack.dataModel.enumClasses.others.SessStatus
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot

class FirestorePaginationManager {

    private val sessionDataCollectionRef = FBase.getUsersSessionsDataCollection()

    private val batchSize = 5L
    private var lastVisibleDocument: QueryDocumentSnapshot? = null
    private var isLoading = false



    fun fetchNextBatch(currentDeviceSession:String, onSuccess: (List<SessData2>) -> Unit, onFailure: (Exception) -> Unit) {
        if (!isLoading) {
            isLoading = true

            // Create a query to get the next batch of records
            var query = sessionDataCollectionRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(batchSize)

            // If there's a lastVisibleDocument, use startAfter to get the next set

            if (lastVisibleDocument != null) {
                query = query.startAfter(lastVisibleDocument!!)
            }
            query.get().addOnSuccessListener { documents ->
                // Check if there are any documents
                if (!documents.isEmpty) {
                    // Update the lastVisibleDocument for the next query
                    lastVisibleDocument = documents.documents[documents.size() - 1] as QueryDocumentSnapshot?
                    val dataList = mutableListOf<SessData2>()
                    // Process the retrieved documents
                    for (document in documents) {
                        if(currentDeviceSession == document.id)
                        {
                            continue
                        }
                        val data = (document.data).toUserSessionData(document.id)
                        if(data.status==SessStatus.LOGGED_OUT)
                        {
                            continue
                        }
                        // Add the data to the list
                        dataList.add(data)
                    }

                    onSuccess(dataList)
                } else {
                    // No more documents available
                    onSuccess(emptyList())
                }

                isLoading = false
            }
            .addOnFailureListener { exception ->
                // Handle failures
                onFailure(exception)
                isLoading = false
            }

        }
    }


}
