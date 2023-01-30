package com.sonde.mentalfitness.domain.usecase

import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.domain.model.User
import com.sonde.mentalfitness.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class SignUpUsecase(val userRepository: UserRepository) {
    suspend fun doSignup(user: User): Flow<Result<Unit>> {
        storeConfigData()
        return userRepository.signupUser(user)
    }

    private fun storeConfigData() {
        val sharedPreferenceHelper =
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        sharedPreferenceHelper.setCheckInConfigData(
            "{\n" +
                    "    \"requestId\": \"4f01171a-c99f-4135-9255-bab23b30fbcf\",\n" +
                    "    \"checkInInstanceId\": \"a982ab18-6e4a-4a9d-afe7-6b9aeb869347\",\n" +
                    "    \"passages\": [\n" +
                    "        {\n" +
                    "            \"prompt\": \"Say Ahh\",\n" +
                    "            \"timerLength\": 6\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"healthchecks\": [\n" +
                    "        {\n" +
                    "            \"name\": \"Mental fitness\",\n" +
                    "            \"sondeplatformName\": \"emotional-resilience\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Concentration\",\n" +
                    "            \"sondeplatformName\": \"emotional_resilience_test\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Engagement\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Optimism\",\n" +
                    "            \"sondeplatformName\": \"nasality\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"questionnaire\": {\n" +
                    "        \"id\": \"qnr-5e61f4715\",\n" +
                    "        \"title\": \"healthcheckapps_Q2_v1\",\n" +
                    "        \"language\": \"en\",\n" +
                    "        \"questions\": [\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_CHOICE\",\n" +
                    "                \"text\": \"How is your breathing today?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Excellent\",\n" +
                    "                        \"score\": 0\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Good Enough\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Some Difficulty\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Challenging\",\n" +
                    "                        \"score\": 3\n" +
                    "                    }" +
                    "                ]\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            } ,\n" +
                    "            {\n" +
                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
                    "                \"text\": \"Are you experiencing any of the following symptoms?\",\n" +
                    "                \"isSkippable\": true,\n" +
                    "                \"options\": [\n" +
                    "                    {\n" +
                    "                        \"text\": \"Wheezing\",\n" +
                    "                        \"score\": 6\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Shortness Of Breath\",\n" +
                    "                        \"score\": 4\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Chest Tightness Or Pain\",\n" +
                    "                        \"score\": 3\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Coughing\",\n" +
                    "                        \"score\": 2\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"Trouble Sleeping\",\n" +
                    "                        \"score\": 1\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"text\": \"None\",\n" +
                    "                        \"score\": 0\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"requestId\": \"9d9faafc-c5e5-43c1-858f-c825d27db9b5\"\n" +
                    "    },\n" +
                    "    \"mainHealthcheckIndex\": 0\n" +
                    "}"
        )


//        sharedPreferenceHelper.setCheckInConfigData(
//            "{\n" +
//                    "    \"requestId\": \"4f01171a-c99f-4135-9255-bab23b30fbcf\",\n" +
//                    "    \"checkInInstanceId\": \"a982ab18-6e4a-4a9d-afe7-6b9aeb869347\",\n" +
//                    "    \"passages\": [\n" +
//                    "        {\n" +
//                    "            \"prompt\": \"Say Ahh\",\n" +
//                    "            \"timerLength\": 6\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"healthchecks\": [\n" +
//                    "        {\n" +
//                    "            \"name\": \"Mental fitness\",\n" +
//                    "            \"sondeplatformName\": \"emotional-resilience\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Concentration\",\n" +
//                    "            \"sondeplatformName\": \"emotional_resilience_test\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Engagement\",\n" +
//                    "            \"sondeplatformName\": \"nasality\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"name\": \"Optimism\",\n" +
//                    "            \"sondeplatformName\": \"nasality\"\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"questionnaire\": {\n" +
//                    "        \"id\": \"qnr-5e61f4715\",\n" +
//                    "        \"title\": \"healthcheckapps_Q2_v1\",\n" +
//                    "        \"language\": \"en\",\n" +
//                    "        \"questions\": [\n" +
//                    "            {\n" +
//                    "                \"type\": \"MULTIPLE_CHOICE\",\n" +
//                    "                \"text\": \"How are you feeling today?\",\n" +
//                    "                \"isSkippable\": true,\n" +
//                    "                \"options\": [\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Sad\",\n" +
//                    "                        \"score\": 0\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Stressed\",\n" +
//                    "                        \"score\": 1\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Unsure\",\n" +
//                    "                        \"score\": 2\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Relaxed\",\n" +
//                    "                        \"score\": 3\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Happy\",\n" +
//                    "                        \"score\": 4\n" +
//                    "                    }\n" +
//                    "                ]\n" +
//                    "            },\n" +
//                    "            {\n" +
//                    "                \"type\": \"MULTIPLE_SELECT\",\n" +
//                    "                \"text\": \"Any specific reasons you would like to share?\",\n" +
//                    "                \"isSkippable\": true,\n" +
//                    "                \"options\": [\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Work\",\n" +
//                    "                        \"score\": 6\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"School\",\n" +
//                    "                        \"score\": 5\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Family\",\n" +
//                    "                        \"score\": 4\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Health\",\n" +
//                    "                        \"score\": 3\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Money\",\n" +
//                    "                        \"score\": 2\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Friends\",\n" +
//                    "                        \"score\": 1\n" +
//                    "                    },\n" +
//                    "                    {\n" +
//                    "                        \"text\": \"Relationship\",\n" +
//                    "                        \"score\": 0\n" +
//                    "                    }\n" +
//                    "                ]\n" +
//                    "            }\n" +
//                    "        ],\n" +
//                    "        \"requestId\": \"9d9faafc-c5e5-43c1-858f-c825d27db9b5\"\n" +
//                    "    },\n" +
//                    "    \"mainHealthcheckIndex\": 0\n" +
//                    "}"
//        )
    }
}