import requests
import json
from requests.packages import urllib3

urllib3.disable_warnings()

class MQRestModule:

    def __init__(self, mq_rest_endpoint):
        self.mq_rest_endpoint = mq_rest_endpoint
        self.sessions={}

    def delete_session(self, sessionid):
        if sessionid in self.sessions:
            del self.sessions[sessionid]
            print('Session '+sessionid+' deleted.')

    def logout(self, sessionId):
        rest_url = self.mq_rest_endpoint + 'logout'
        print("SessionId:"+sessionId)
        print("session-map:"+str(self.sessions))
        response = requests.get(rest_url, cookies=self.sessions[sessionId], verify=False)
        if response.status_code != 200:
            raise Exception('Error in contacting cowbrow:' + str(response.content))
        self.delete_session(sessionid=sessionId)


    def login(self, server_map):
        addstuff = '?host='+server_map['host']
        if 'user' in server_map and not server_map['user']==None:
            addstuff+='&user='+server_map['user']+"&password="+server_map['password']
        if 'port' in server_map and not server_map['port']==None:
            addstuff+='&port='+server_map['port']
        print(self.mq_rest_endpoint + 'login' + addstuff)
        cookies = requests.get(self.mq_rest_endpoint + 'login' + addstuff, verify=False).cookies

        print("Server "+self.mq_rest_endpoint+" opened.")

        self.sessions[cookies.get_dict()['JSESSIONID']]=cookies.get_dict()

        return cookies.get_dict()['JSESSIONID']

    def get_num_messages(self, queuename, session_id):
        print('Getting queue depth for queue:'+queuename)
        js = self.get_queues(sessionid=session_id)

        for queue in js['responses']:
            if queue['name']==queuename:
                return queue['numMessages']

        return -1

    def get_messages_from_queue(self, queuename, start_index, end_index, session_id):
        if not int(start_index)==0:
            print("startindex=" + str(start_index))
            raise Exception("Paging not yet implemented.")
        rest_url=self.mq_rest_endpoint + 'messages?queue=' + queuename+"&messagestoshow="+str(end_index)+"&payloadcutoff=2000"
        print(rest_url)
        response=requests.get(rest_url, cookies=self.sessions[session_id], verify=False)
        if response.status_code!=200:
            raise Exception('Error in contacting cowbrow:'+str(response.content))
        messages=None
        try:
            messages = response.json()
        except Exception as e:
            print('Error:'+str(e)+' Content'+response.content)
        return messages


    def get_queues(self, sessionid):
        #print(self.mq_rest_endpoint+"destinations")
        print("Cookies are: "+json.dumps(self.sessions[sessionid]))

        response=requests.get(self.mq_rest_endpoint+"destinations", cookies=self.sessions[sessionid], verify=False, timeout=10000)
        print('response gotten.')
        ret_js= None
        try:
            ret_js=response.json()
        except Exception as e:
            print(str(e)+". Content:"+str(response.content))
            raise
        return ret_js

    def get_message_with_id(self, queuename, messageid, sessionid):

        # THIS IS PURELY FOR DEVELOPING PURPOSES:
        response = self.get_messages_from_queue(queuename=queuename, start_index=0, end_index=2000, session_id=sessionid)

        for message in response['responses']:
            #print(message)
            if message['headers']['JMSMessageID']==messageid:
                return message
        # END OF THIS

        return None
