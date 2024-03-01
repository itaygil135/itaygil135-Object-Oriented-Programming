public class Chat
{
    public static void main(String[] args)
    {
        ChatterBot[] chatterBotArray = new ChatterBot[2];

//        String[] arr1 = {"what" + ChatterBot.ILLEGAL_REQUEST_PLACEHOLDER, "say I should say"};
//        String[] arr2 = {"whaaat" + ChatterBot.ILLEGAL_REQUEST_PLACEHOLDER, "say say"};
//
//        String[] arr3 = {"say t3.1" + ChatterBot.REQUESTED_PHRASE_PLACEHOLDER, " koko saay t3.2" +
//                " <phrase>"};
//        String[] arr4 = {"say t4.1 <phrase>", " koko saay t4.2 <phrase>"};

        String[] arr1 = {"what: " + ChatterBot.ILLEGAL_REQUEST_PLACEHOLDER};
        String[] arr2 = {"say ->" + ChatterBot.ILLEGAL_REQUEST_PLACEHOLDER};

        String[] arr3 = {"say say " + ChatterBot.REQUESTED_PHRASE_PLACEHOLDER};
        String[] arr4 = {"I am saying: " + ChatterBot.REQUESTED_PHRASE_PLACEHOLDER};

        ChatterBot chat1 = new ChatterBot("Bot1", arr1, arr3);
        ChatterBot chat2 = new ChatterBot("Bot2",arr2, arr4);

        chatterBotArray[0] = chat1;
        chatterBotArray[1] = chat2;

        String statement = "Hi";

        //while(true)
        for (int i =0 ; i < 4; i++)
        {
            for(ChatterBot bot : chatterBotArray)
            {
                String statement1= bot.replyTo(statement);
                String botName = bot.getName();
                System.out.println(botName +": " + statement1);
                statement = statement1;


            }
        }

    }
}
