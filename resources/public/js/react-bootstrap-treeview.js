var TreeView = React.createClass(
    {
        displayName: "TreeView",

        propTypes: {
            levels: React.PropTypes.number,
            expandIcon: React.PropTypes.string,
            collapseIcon: React.PropTypes.string,
            emptyIcon: React.PropTypes.string,
            nodeIcon: React.PropTypes.string,
            nodeIconSelected: React.PropTypes.string,

            color: React.PropTypes.string,
            backColor: React.PropTypes.string,
            borderColor: React.PropTypes.string,
            selectedColor: React.PropTypes.string,
            selectedBackColor: React.PropTypes.string,

            enableLinks: React.PropTypes.bool,
            highlightSelected: React.PropTypes.bool,
            isSelectionExclusive: React.PropTypes.bool,
            underlineLeafOnly: React.PropTypes.bool,
            showBorder: React.PropTypes.bool,
            showTags: React.PropTypes.bool,

            data: React.PropTypes.arrayOf(React.PropTypes.object),
            onLineClicked: React.PropTypes.func
        },

        getDefaultProps: function () {
            return {
                levels: 2,

                expandIcon: 'glyphicon glyphicon-plus',
                collapseIcon: 'glyphicon glyphicon-minus',
                emptyIcon: '',
                nodeIcon: 'glyphicon glyphicon-stop',
                nodeIconSelected: 'glyphicon glyphicon-eye-open',
                color: undefined,
                backColor: undefined,
                borderColor: undefined,
                selectedColor: '#AAAAAA',
                selectedBackColor: '#428bca',
                classText: '',

                enableLinks: false,
                highlightSelected: true,
                isSelectionExclusive: true,
                underlineLeafOnly: false,
                showBorder: false,
                showTags: false,

                data: []
            }
        },

        nodes: [],
        nodesSelected: {},

        getInitialState: function () {
            // this.setNodeId({nodes: this.props.data});

            return {nodesSelected: this.nodesSelected};
        },

        setNodeId: function (node) {
            console.log("node id");
            console.log(node);

            // if (!node.nodes) {
            //     return;
            // }

            // node.nodes.forEach(function checkStates(node) {
            //     node.nodeId = node.path;
            //
            //     this.nodesSelected[node.nodeId] = false;
            //     this.nodes.push(node);
            //     this.setNodeId(node);
            // }, this);
        },

        findNode: function (nodeId) {
            var find = {};
            this.nodes.forEach(function (node) {
                if (node.nodeId === nodeId) {
                    find = node;
                }
            });
            return find;
        },

        handleLineClicked: function (nodeId, evt) {
            if (this.props.onLineClicked !== undefined) {
                this.props.onLineClicked($.extend(true, {}, evt));
            }

            var matrice = this.state.nodesSelected;
            // Exclusive selection
            if (this.props.isSelectionExclusive) {

                // Underline only if the element is a leaf
                if (this.props.underlineLeafOnly) {
                    var currentNode = this.findNode(nodeId);

                    // Node clicked is a leaf
                    if (!currentNode.nodes) {
                        // Unselection
                        for (var i in matrice) {
                            matrice[i] = false;
                        }
                        matrice[nodeId] =
                            !this.state.nodesSelected[nodeId];
                    }
                    // Node clicked is a parentNode
                    else {
                        // Simulation click expand/collapse icon
                        $(evt.currentTarget).find(
                            '[data-target=plusmoins]').click();
                    }
                }
                // Underline on all nodes
                else {
                    // Unselection
                    for (var i in matrice) {
                        matrice[i] = false;
                    }
                    // TOGGLE SELECTION OF CURRENT NODE
                    matrice[nodeId] =
                        !this.state.nodesSelected[nodeId];
                }
            }
            // MULTIPLE SELECTION
            else {
                // TOGGLE SELECTION OF CURRENT NODE
                matrice[nodeId] = !this.state.nodesSelected[nodeId];
            }

            this.setState({nodesSelected: matrice});
        },

        render: function () {

            var children = [];
            if (this.props.data) {
                this.props.data.forEach(function (node, index) {

                    node.selected = (this.state.nodesSelected[node.nodeId]);

                    children.push(
                        React.createElement(TreeNode, {
                            node: node,
                            level: 1,
                            visible: true,
                            options: this.props,
                            key: node.nodeId,
                            onLineClicked: this.handleLineClicked,
                            nodesSelected: this.state.nodesSelected
                        }));
                }.bind(this));
            }

            return (
                React.createElement(
                    "div",
                    {className: "treeview"},
                    React.createElement(
                        "ul",
                        {className: "list-group"},
                        children
                    )
                )
            );
        }
    }
);

var TreeNode = React.createClass(
    {
        displayName: "TreeNode",

        propTypes: {
            node: React.PropTypes.object.isRequired,
            onLineClicked: React.PropTypes.func,
            nodesSelected: React.PropTypes.object.isRequired,
            options: React.PropTypes.object
        },

        getInitialState: function () {
            var node = this.props.node;
            console.log(node);
            return {
                expanded: (node.state && node.state.hasOwnProperty('expanded'))
                    ? node.state.expanded : (this.props.level < this.props.options.levels),
                selected: (node.state && node.state.hasOwnProperty('selected'))
                    ? node.state.selected : false,
                key: node.path
            }
        },

        componentWillUpdate: function (np, ns) {
            ns.selected = np.node.selected;

        },

        toggleExpanded: function (id, event) {
            this.setState({expanded: !this.state.expanded});
            event.stopPropagation();
        },

        toggleSelected: function (id, event) {
            // Exclusive selection
            if (!this.props.isSelectionExclusive) {
                this.setState({selected: !this.state.selected});
            }
            event.stopPropagation();
        },

        handleLineClicked: function (nodeId, evt) {

            // SELECT LINE
            this.toggleSelected(nodeId, $.extend(true, {}, evt));
            // DEV CLICK
            this.props.onLineClicked(nodeId, $.extend(true, {}, evt));
            evt.stopPropagation();
        },

        render: function () {
            var node = this.props.node;
            var options = this.props.options;

            var style;
            if (!this.props.visible) {
                style = {
                    display: 'none'
                };
            }
            else {

                if (options.highlightSelected && this.state.selected) {
                    style = {
                        color: options.selectedColor,
                        backgroundColor: options.selectedBackColor
                    };
                }
                else {
                    style = {
                        color: node.color || options.color,
                        backgroundColor: node.backColor
                                         || options.backColor
                    };
                }

                if (!options.showBorder) {
                    style.border = 'none';
                }
                else if (options.borderColor) {
                    style.border = '1px solid ' + options.borderColor;
                }
            }

            // Indentation
            var indents = [];
            for (var i = 0; i < this.props.level - 1; i++) {
                indents.push(React.createElement("span", {
                    className: "indent",
                    key: i
                }));
            }

            var expandCollapseIcon;
            // There are children
            if (node.nodes) {
                // Collapse
                if (!this.state.expanded) {
                    expandCollapseIcon = (
                        React.createElement("span",
                                            {className: "icon plusmoins"},
                                            React.createElement("i", {
                                                className: options.expandIcon,
                                                onClick: this.toggleExpanded.bind(this,
                                                                                  node.nodeId),
                                                "data-target": "plusmoins"
                                            })
                        )
                    );
                }
                // Expanded
                else {
                    expandCollapseIcon = (
                        React.createElement(
                            "span",
                            {className: "icon"},
                            React.createElement(
                                "i",
                                {
                                    className: options.collapseIcon,
                                    onClick: this.toggleExpanded.bind(this, node.nodeId),
                                    "data-target": "plusmoins"
                                }
                            )
                        )
                    );
                }
            }
            // Node is a leaf
            else {
                expandCollapseIcon = (
                    React.createElement(
                        "span",
                        {className: options.emptyIcon}
                    )
                );
            }

            // Icon (if current node is a leaf)
            var nodeIcon = '';
            if (options.nodeIcon !== '' && !node.nodes) {
                //console.log('node %o %o %o',node,
                // this.state.selected, options);
                var iTarget = (React.createElement("i", {
                    className: node.icon || options.nodeIcon
                }));
                // Current node selected
                if (this.state.selected) {
                    iTarget =
                        (React.createElement("i",
                                             {className: options.nodeIconSelected}))
                }
                nodeIcon = (
                    React.createElement("span", {className: "icon"},
                                        iTarget
                    )
                );
            }

            var badges = '';
            if (options.showTags) {
                // If tags are defined in the data
                if (node.tags) {
                    badges = node.tags.map(function (tag, index) {
                        return (
                            React.createElement("span", {
                                                    className: "badge",
                                                    key: index
                                                },
                                                tag
                            )
                        );
                    });
                }
                // No tags in data => number of children
                else {
                    // Children exist
                    if (node.nodes) {
                        badges = (
                            React.createElement("span", {
                                                    className: "badge"
                                                },
                                                node.nodes.length
                            )
                        );
                    }
                }
            }

            var nodeText;
            if (options.enableLinks) {
                nodeText = (
                    React.createElement("span", {
                                            className: options.classText
                                        },
                                        React.createElement("a",
                                                            {href: node.href/*style="color:inherit;"*/},
                                                            node.text
                                        )
                    )
                );
            }
            else {
                nodeText = (
                    React.createElement("span", {
                                            className: options.classText
                                        },
                                        node.text
                    )
                );
            }

            var children = [];
            if (node.nodes) {
                node.nodes.forEach(function (node, index) {
                    node.selected = (this.props.nodesSelected[node.nodeId]);
                    children.push(
                        React.createElement(TreeNode, {
                            node: node,
                            level: this.props.level + 1,
                            visible: this.state.expanded && this.props.visible,
                            options: options,
                            key: this.state.key,
                            onLineClicked: this.props.onLineClicked,
                            nodesSelected: this.props.nodesSelected
                        }));
                }, this);
            }

            return (
                React.createElement(
                    "li",
                    {
                        className: "list-group-item",
                        style: style,
                        onClick: this.handleLineClicked.bind(this, node.nodeId)
                    },
                    indents,
                    expandCollapseIcon,
                    nodeIcon,
                    nodeText,
                    badges,
                    React.createElement(
                        "ul",
                        {},
                        children
                    )
                )
            );
        }
    }
);
