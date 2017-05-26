from subprocess import Popen, PIPE

# Directory and volume stuff
def call_out(cmd_list):
    p = Popen(cmd_list, stdin=PIPE, stdout=PIPE, stderr=PIPE)
    output, err = p.communicate(b"input data that is passed to subprocess' stdin")
    p_status=p.wait()

    rc = p.returncode
    return output.decode('utf-8'), str(err), p.returncode
