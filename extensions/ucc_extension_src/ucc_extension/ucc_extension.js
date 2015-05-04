var callbacks = [];

extension.setMessageListener(function (msg) {
                                 // each callback only gets invoked once; we delete it after
                                 // calling it
                                 for (var i = 0; i < callbacks.length; i += 1) {
                                     callbacks[i](msg);
                                     delete callbacks[i];
                                 };
                             });

exports.read_system_config = function (callback) {
    callbacks.push(callback);
    extension.postMessage("read_sys_conf");
};

exports.read_system_configSync = function () {
    return extension.internal.sendSyncMessage("read_sys_conf");
};
