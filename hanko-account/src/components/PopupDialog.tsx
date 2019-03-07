import * as React from 'react'

export class PopupDialog extends React.Component {
  render() {
    return (
      <div className="popup-wrapper">
        <div className="popup-content">{this.props.children}</div>
      </div>
    )
  }
}
