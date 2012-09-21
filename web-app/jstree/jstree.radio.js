/*
 * jsTree radio plugin 1.0
 * Inserts radio button in front of every node
 * Depends on the ui plugin
 */

/*
 * TODO : append "disable_nodes" option (not recursively feature)
 * TODO : always show selected node (user can't close parent folder)
 */

(function ($) {
    $.jstree.plugin("radio", {
        __init : function () {
            var s = this._get_settings().radio;
            this.empty_selection = s.empty_selection;
            this.select_node = this.deselect_node = this.deselect_all = $.noop;
            this.get_selected = this.get_checked;

            this.get_container()
                .bind("open_node.jstree create_node.jstree clean_node.jstree", $.proxy(function (e, data) {
                this._prepare_radioboxes(data.rslt.obj);
            }, this))
                .bind("loaded.jstree", $.proxy(function (e) {
                this._prepare_radioboxes();

                if (s.initially_select != null) {
                    this._select_node_by_id(s.initially_select);
                }

            }, this))
                .delegate("a", "click.jstree", $.proxy(function (e) {
                if(this._get_node(e.target).hasClass("jstree-checked")) { this.uncheck_node(e.target); }
                else { this.check_node(e.target); }
                if(this.data.ui) { this.save_selected(); }
                if(this.data.cookies) { this.save_cookie("select_node"); }
                e.preventDefault();
            }, this));

        },
        defaults : {
            empty_selection: true,
            initially_select: null,
            ajax : false,
            valid_selection: null,
            disable_nodes_recusively: null
        },
        __destroy : function () {
            this.get_container().find(".jstree-radio").remove();
        },
        _fn : {
            _prepare_radioboxes : function (obj) {
                var valid_selection = this._get_settings().radio.valid_selection;
                var disable_nodes_recusively = this._get_settings().radio.disable_nodes_recusively;

                obj = !obj || obj == -1 ? this.get_container() : this._get_node(obj);
                var c, _this = this, t;
                obj.each(function () {
                    t = $(this);

                    if (t.is("li")) {
                        t.find("a").not(":has(.jstree-radio)").each(function(node) {
                            var li_node = $(this).parent();
                            var type = li_node.attr('rel');
                            if (type == undefined)
                                type = 'default';

                            var _disable_node = false;
                            if (disable_nodes_recusively != null) {
                                if (disable_nodes_recusively.indexOf(li_node.attr('id')) > -1) {
                                    _disable_node = true;
                                } else {
                                    li_node.parents('LI').each(function() {
                                        if (disable_nodes_recusively.indexOf($(this).attr('id')) > -1) {
                                            _disable_node = true;
                                            return false;
                                        }
                                    });
                                }
                            }

                            if (
                                (
                                    (valid_selection == null) ||
                                        (valid_selection.indexOf(type)!=-1)
                                    ) && (
                                    (!_disable_node)
                                    )
                                ) {
                                $(this)
                                    .prepend("<ins class='jstree-radio'>&#160;</ins>")
                                    .parent()
                                    .not(".jstree-checked, .jstree-unchecked")
                                    .addClass("jstree-unchecked");
                            }
                        });
                    }
                });
            },
            _select_node_by_id : function(node_id) {
                var node = $('#' + node_id).get(0)
                var _this = this;
                if (node == undefined) {
                    s = this.get_settings().radio;
                    if (s.ajax == false) return;

                    s.ajax.context = this;
                    s.ajax.error = function() { };
                    s.ajax.success = function(data, textstatus, XMLHttpRequest) {
                        _this.to_open = data;
                        _this.__open_nodes(function() {
                            _this._select_node_by_id(node_id);
                        });
                    };

                    if($.isFunction(s.ajax.url)) { s.ajax.url = s.ajax.url.call(this, node_id); }
                    if($.isFunction(s.ajax.data)) { s.ajax.data = s.ajax.data.call(this, node_id); }
                    if(!s.ajax.dataType) { s.ajax.dataType = "json"; }

                    $.ajax(s.ajax);
                    return;
                } else {
                    this.data.core.to_open = this.get_path(node, true);
                    this.reopen();
                    this.check_node(node);
                }
            },
            __open_nodes: function(callback) {
                if (this.to_open.length) {
                    var node_id = this.to_open.shift();
                    var node = $('#'+node_id).get(0);
                    var _this = this;
                    this.open_node(
                        node,
                        function() {
                            _this.__open_nodes(callback);
                        }
                    );
                } else {
                    callback();
                }
            },
            change_state : function (obj, state) {
                if (obj == undefined)
                    return;

                obj = this._get_node(obj);
                if (!$("> A > .jstree-radio", obj).length > 0)
                    return;

                if (this.empty_selection == false)
                    state = false;

                this.get_checked().each(function() {
                    $(this).removeClass("jstree-checked jstree-undetermined").addClass("jstree-unchecked");
                });
                state = (state === false || state === true) ? state : obj.hasClass("jstree-checked");
                if(state) { obj.find("li").andSelf().removeClass("jstree-checked jstree-undetermined").addClass("jstree-unchecked"); }
                else {
                    obj.removeClass("jstree-unchecked jstree-undetermined").addClass("jstree-checked");
                    if(this.data.ui) { this.data.ui.last_selected = obj; }
                    this.data.radio.last_selected = obj;
                }
                if(this.data.ui) { this.data.ui.selected = this.get_checked(); }
                this.__callback(obj);
            },
            check_node : function (obj) {
                this.change_state(obj, false);
            },
            uncheck_node : function (obj) {
                this.change_state(obj, true);
            },
            uncheck_all : function () {
                if (this.empty_selection == false)
                    return;

                var _this = this;
                this.get_container().children("ul").children("li").each(function () {
                    _this.change_state(this, true);
                });
            },

            is_checked : function(obj) {
                obj = this._get_node(obj);
                return obj.length ? obj.is(".jstree-checked") : false;
            },
            get_checked : function (obj) {
                obj = !obj || obj === -1 ? this.get_container() : this._get_node(obj);
                return obj.find(".jstree-checked");
            },
            get_unchecked : function (obj) {
                obj = !obj || obj === -1 ? this.get_container() : this._get_node(obj);
                return obj.find("> ul > .jstree-unchecked, .jstree-undetermined > ul > .jstree-unchecked");
            },

            show_checkboxes : function () { this.get_container().children("ul").removeClass("jstree-no-checkboxes"); },
            hide_checkboxes : function () { this.get_container().children("ul").addClass("jstree-no-checkboxes"); },

            reselect : function () {
                if(this.data.ui) {
                    var _this = this,
                        s = this.data.ui.to_select;
                    s = $.map($.makeArray(s), function (n) { return "#" + n.toString().replace(/^#/,"").replace('\\/','/').replace('/','\\/'); });
                    this.deselect_all();
                    $.each(s, function (i, val) { _this.check_node(val); });
                    this.__callback();
                }
            }
        }
    });
})(jQuery);
//*/