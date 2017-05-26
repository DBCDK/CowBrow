import json
from utils.cmd_util import call_out

# DEPRECATED
#
# Use rest version of cowbrow
#


class mq_module:

    def __init__(self):
        print('init mq module')
        self.cmd = ["java", "-jar", "cowbrow.jar"]

    def perform_cowbrow_request(self, cmd_list):
        out, err, code = call_out(cmd_list=cmd_list)
        if code==0:
            ret_js = json.loads(out)
            if ret_js['statusCode']==200:
                return ret_js
            else:
                raise Exception('Cowbrow returned statuscode:' + str(ret_js['statusCode'])+' with return string:'+ str(out))
        else:
            raise Exception('Error running cowbrow:'+str(err))


    def get_queues(self, server_map):
        return  self.perform_cowbrow_request( cmd_list=self.cmd +
                                                       [ '-H',
                                                         server_map['host'],
                                                         '-p',
                                                         server_map['port'],
                                                         'list'
                                                         ]
                                              )

    def get_messages_from_queue_partial(self, server_map, queuename, start_index, end_idex):
        mess = self.get_messages_from_queue(server_map=server_map, queuename=queuename)

        #### DEBUG FILL UP ####
        if len(mess['responses'])>0:
            message = mess['responses'][0]
            for i in range(1000):
                mess['responses'].append(message)
        #### END DEBUG FILL UP ####

        #### DEBUG CUT OFF BODY AT 10000 ####
        for message in mess['responses']:
            message['payload']=message['payload'][0:4000]
        #### END DEBUG CUT OFF BODY AT 4000 ####

        print(json.dumps(mess['responses'][0], indent=2))
        print("Number of messages in queue:"+str(len(mess['responses'])))
        print("Showing "+str(start_index)+" to "+str(end_idex))
        mess['responses'] = mess['responses'][start_index:end_idex]
        self.messagelist = mess
        return mess


    def get_messages_from_queue(self, server_map, queuename):
        return self.perform_cowbrow_request( cmd_list=self.cmd +
                                                      [
                                                          '-H',
                                                          server_map['host'],
                                                          '-p',
                                                          server_map['port'],
                                                          'listmessages',
                                                          '-q',
                                                          queuename
                                                      ]
                                             )

    def get_message_with_id(self, id):
        for mess in self.messagelist['responses']:
            if mess['headers']['JMSMessageID']==id:
                return mess


    def sort_props(self, props):
        sorted={}
        for prop in props:
            sorted[prop['key']]=prop['value']
        return sorted

    def reorder_properties_as_map(self, server_map, queuename):
        for message in self.get_messages_from_queue(server_map=server_map, queuename=queuename)['responses']:
            message['properties']=self.sort_props(props=message['properties'])
