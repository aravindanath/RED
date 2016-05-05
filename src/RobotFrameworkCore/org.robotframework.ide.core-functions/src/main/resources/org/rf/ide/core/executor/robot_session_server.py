#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#
# Author: Mateusz Marzec
#
from sys import __excepthook__

    
class Logger(object):
    def log(self, message):
        sys.stdout.write(message + '\n')
        sys.stdout.flush()
        
    def log_error(self, message):
        sys.stderr.write(message + '\n')
        sys.stderr.flush()

def encode_result_or_exception(func):
    import traceback
    def inner(*args, **kwargs):
        result = {'result': None, 'exception' : None}
        try:
            result['result'] = func(*args, **kwargs)
            return result
        except: 
            msg = traceback.format_exc()
            result['exception'] = msg
            Logger().log_error(msg)
            return result
    return inner    
 
def logargs(func):
    from datetime import datetime
    def inner(*args, **kwargs):
        current_time = datetime.now().strftime('%H:%M:%S.%f')[:-3]
        
        msg = '[' + current_time + '] calling \'' + func.__name__ + '\' function, '
        if args == None or len(args) == 0:
            msg = msg + 'no arguments'
        else:
            msg = msg + 'supplied arguments:\n' + '\n'.join(map(lambda arg : '    > ' + str(arg), args))
        Logger().log(msg)
        return func(*args, **kwargs)
    return inner

def logresult(func):
    from datetime import datetime
    def inner(*args, **kwargs):
        ret = func(*args, **kwargs)
        current_time = datetime.now().strftime('%H:%M:%S.%f')[:-3]
        
        if ret['exception']: 
            Logger().log('[' + current_time + '] call ended with exception, see stderr for details')
        else:
            Logger().log('[' + current_time + '] call ended with result:\n    > ' + str(ret['result']))
        return ret
    return inner


@logresult
@encode_result_or_exception
@logargs
def check_server_availability():
    pass


@logresult
@encode_result_or_exception
@logargs
def get_modules_search_paths():
    import red_modules
    return red_modules.get_modules_search_paths()


@logresult
@encode_result_or_exception
@logargs
def get_module_path(module_name, python_paths, class_paths):
    import red_modules
    import platform
    from robot import pythonpathsetter

    __extend_classpath(class_paths)
    
    for path in python_paths + class_paths:
        pythonpathsetter.add_path(path)    
    module_path = red_modules.get_module_path(module_name)
    for path in python_paths + class_paths:
        pythonpathsetter.remove_path(path)    
    return module_path


@logresult
@encode_result_or_exception
@logargs
def get_variables(dir, args):
    import red_variables
    return red_variables.get_variables(dir, args)


@logresult
@encode_result_or_exception
@logargs
def get_global_variables():
    import red_variables
    return red_variables.get_global_variables()


@logresult
@encode_result_or_exception
@logargs
def get_standard_libraries_names():
    import red_libraries
    return red_libraries.get_standard_library_names()


@logresult
@encode_result_or_exception
@logargs
def get_standard_library_path(libname):
    import red_libraries
    return red_libraries.get_standard_library_path(libname)


@logresult
@encode_result_or_exception
@logargs
def get_robot_version():
    return _get_robot_version()
    
def _get_robot_version():
    try:
        import robot
    except ImportError:
        return None
    from robot import version
    return 'Robot Framework ' + version.get_full_version()


@logresult
@encode_result_or_exception
@logargs
def get_run_module_path():
    import red_modules
    return red_modules.get_run_module_path()


@logresult
@encode_result_or_exception
@logargs
def create_libdoc(result_filepath, libname, python_paths, class_paths):
    import robot
    from robot import pythonpathsetter
    from robot.libdoc import libdoc

    __extend_classpath(class_paths)
        
    for path in python_paths + class_paths:
        pythonpathsetter.add_path(path)    
    libdoc(libname, result_filepath, format='XML')
    for path in python_paths + class_paths:
        pythonpathsetter.remove_path(path)    

def __extend_classpath(class_paths):
    import platform

    if platform.python_implementation() == 'Jython':
        for class_path in class_paths:
            from classpath_updater import ClassPathUpdater
            cp_updater = ClassPathUpdater()
            cp_updater.add_file(class_path) 

def __shutdown_server_when_parent_process_becomes_unavailable(server):
    import sys

    # this causes the function to block on readline() call; parent process which 
    # started this script shouldn't write anything to the input, so this function will
    # be blocked until parent process will be closed/killed; this will cause readline()
    # to read EOF and hence proceed to server.shutdown() which will terminate whole script
    sys.stdin.readline()
    server.shutdown()


if __name__ == '__main__':
    import socket
    socket.setdefaulttimeout(10)
    
    import sys
    from threading import Thread
    try:
        from xmlrpc.server import SimpleXMLRPCServer
    except ImportError:
        from SimpleXMLRPCServer import SimpleXMLRPCServer
    
    IP = '127.0.0.1'
    PORT = int(sys.argv[1])

    server = SimpleXMLRPCServer((IP, PORT), allow_none=True)
    server.register_function(get_modules_search_paths, 'getModulesSearchPaths')
    server.register_function(get_module_path, 'getModulePath')
    server.register_function(get_variables, 'getVariables')
    server.register_function(get_global_variables, 'getGlobalVariables')
    server.register_function(get_standard_libraries_names, 'getStandardLibrariesNames')
    server.register_function(get_standard_library_path, 'getStandardLibraryPath')
    server.register_function(get_robot_version, 'getRobotVersion')
    server.register_function(get_run_module_path, 'getRunModulePath')
    server.register_function(create_libdoc, 'createLibdoc')
    server.register_function(check_server_availability, 'checkServerAvailability')

    red_checking_thread = Thread(target=__shutdown_server_when_parent_process_becomes_unavailable, args={server})
    red_checking_thread.setDaemon(True)
    red_checking_thread.start()

    robot_ver = _get_robot_version()
    logger = Logger()
    logger.log('# RED session server started @' + str(PORT))
    logger.log('# python version: ' + sys.version)
    logger.log('# robot version: ' + (robot_ver if robot_ver else "<no robot installed>"))
    logger.log('# script path: ' + __file__)
    logger.log('\n')
    
    server.serve_forever()
