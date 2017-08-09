import requests
from flask import Flask, render_template, session, request
from mq_python.mqrestmodule import MQRestModule
from utils.ad_hoc_decode import ad_hoc_base64_decode_content
import os
import random
import string

#
# FLASK and defaulting stuff
app = Flask(__name__)
app.config.from_object(__name__) # load config from this file , flaskr.py

print(os.environ.get('CBW_CONF'))
app.config.update(dict(
    requests.get( url=os.environ.get('CBW_CONF') ).json()
))


mq_rest=MQRestModule(mq_rest_endpoint=app.config['mq_rest_gateway'])

@app.route('/')
def landing_page():
    return render_template('landing_page.html', servers=app.config['mq_backends'], whereami="servers /")

@app.route('/server/<servername>')
def list_queues(servername):

    try:
        if mq_rest.sessions!={} and "JSESSIONID" in session and session['JSESSIONID']!=None:
            print("Logout for session "+session['JSESSIONID'])
            mq_rest.logout(session['JSESSIONID'])

        sessionid=str(mq_rest.login(server_map=app.config['mq_backends'][servername]))

        session['JSESSIONID']=sessionid
        session['server_id']=servername
        qs=mq_rest.get_queues(sessionid=sessionid)
        print('queues fetched for server '+servername)
    except Exception as e:
        print("Exception when fetching data for server "+servername+". Error:"+str(e))
        return render_template('error.html', err_msg=str(e))

    return render_template('list_queues.html', qs=qs, whereami="servers / "+servername, servername=servername)

@app.route('/do_other_server', methods=['POST'])
def do_other_server():

    if request.form['username']==None or \
                    request.form['password']==None or \
                    request.form['port']==None or \
                    request.form['servername']==None:
        return render_template('error.html', err_msg='No user form input')
    server_name = request.form['servername']

    server_map = { 'user':request.form['username'],
                   'password': request.form['password'],
                   'port':request.form['port'],
                   'host':server_name
                   }
    sessionid=str(mq_rest.login( server_map ))
    print('logged in to '+str())
    session['JSESSIONID'] = sessionid
    session['server_id'] = server_name
    try:
        qs = mq_rest.get_queues(sessionid=sessionid)
        print('queues fetched for server ' + server_name)

    except Exception as e:
        print("Exception when fetching data for server " + server_name + ". Error:" + str(e))
        return render_template('error.html', err_msg=str(e))
    return render_template('list_queues.html', qs=qs, whereami="servers / " + server_name, servername=server_name)


@app.route('/other_server')
def other_server():
    return render_template('other_server_login.html', whereami="servers /")

@app.route('/view_queue/<servername>/<queuename>/<startindex>/<endindex>')
def view_queue(servername, queuename, startindex, endindex):
    try:
        messages=mq_rest.get_messages_from_queue(queuename=queuename,
                                                 start_index=startindex,
                                                 end_index=endindex,
                                                 session_id=session['JSESSIONID']
                                                 )
        total_num_messages=mq_rest.get_num_messages( queuename=queuename,
                                                     session_id=session['JSESSIONID']
                                                     )
        return render_template( 'list_messages.html',
                                messages=messages,
                                servername=servername,
                                queue=queuename,
                                whereami='servers /'+session['server_id']+' / '+queuename+' (0-'+str(len(messages['responses']))+')',
                                total_num_messages=total_num_messages
                                )
    except Exception as e:
        print("Exception when fetching queue content for queue:"+servername+"/"+queuename)
        return render_template('error.html', err_msg=str(e))


def split_map_in_two_columns(table_of_maps):
    rearranged = []
    _line = []
    for k, v in table_of_maps.items():
        if len(_line) == 0:
            _line = [{k: v}]
        else:
            _line.append({k: v})
            rearranged.append(_line)
            _line=[]

    return rearranged

def flatify(flated_map):
    deflated={}
    for entry in flated_map:
        deflated[ entry['key'] ]= entry['value']
    return deflated


@app.route('/view_message/<queuename>/<messageid>')
def view_message( queuename, messageid ):
    try:
        message=mq_rest.get_message_with_id(messageid=messageid, queuename=queuename, sessionid=session['JSESSIONID'])
        if message==None:
            raise Exception('Message with id '+str(messageid)+' not found ')

        # Massage message to enable easy rendering
        message["arranged_headers"] = split_map_in_two_columns(table_of_maps=message['headers'])
        message["arranged_properties"] = split_map_in_two_columns(flatify(message['properties']))


        # Look for base64 encoded content, and give a go of decoding. Not always succesful, utf-8 problems.
        try:
            message["base64_decoded_content"] = ad_hoc_base64_decode_content(message['payload'])


        except Exception as e:
            print('Exception from base64 decode content: '+str(e))
            print('Payload:'+str(message['payload']))
            if message['payload']==None:
                message["base64_decoded_content"]=''
            else:
                message["base64_decoded_content"]=message['payload']




        return render_template('view_message.html', message=message, whereami='servers / '+session['server_id']+' / '+queuename)
    except Exception as e:
        print("Exception from message-generator:"+str(e))
        return render_template('error.html', err_msg=str(e)+' for message with id '+str(messageid))



if __name__ == "__main__":

    app.secret_key= ''.join(random.choice(string.ascii_letters) for i in range(32))
    print(app.secret_key)
    if 'SSL' in os.environ and os.environ['SSL']=='yes':
        context = ('server.crt', 'server.key')
        app.run(port=5000, host='0.0.0.0', ssl_context=context, threaded=True)
    else:
        app.run( port=5000, host='0.0.0.0', threaded=True)