/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import type { IconName, Intent } from '@blueprintjs/core';
import { Menu, MenuItem } from '@blueprintjs/core';
import type { JSX } from 'react';
import React from 'react';

export interface BasicAction {
  icon?: IconName;
  title: string;
  intent?: Intent;
  onAction: () => void;
}

export function basicActionsToMenu(basicActions: BasicAction[]): JSX.Element | undefined {
  if (!basicActions.length) return;
  return (
    <Menu>
      {basicActions.map((action, i) => (
        <MenuItem
          key={i}
          icon={action.icon}
          text={action.title}
          intent={action.intent}
          onClick={action.onAction}
        />
      ))}
    </Menu>
  );
}